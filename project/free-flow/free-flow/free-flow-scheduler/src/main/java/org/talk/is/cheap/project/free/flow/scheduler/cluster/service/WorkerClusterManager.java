package org.talk.is.cheap.project.free.flow.scheduler.cluster.service;

import io.vavr.Tuple2;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.client.WorkerClusterClient;
import org.talk.is.cheap.project.free.flow.common.enums.NodeAction;
import org.talk.is.cheap.project.free.flow.common.enums.NodeType;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.event.WorkerTerminatedEvent;
import org.talk.is.cheap.project.free.flow.scheduler.config.property.ZKPathProperty;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNodeLog;
import org.talk.is.cheap.project.free.flow.starter.repository.service.ClusterNodeLogService;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 这个是leader scheduler要做的事情：
 * 1. 监听worker的上下线，并关注worker的健康情况；
 * 2. 通过zk将worker节点情况同步给其他scheduler，这样让其他的scheduler也能执行任务调度、分配、启动、状态流转
 * <p>
 * 一个worker的注册整体流程大体分为下列几步
 * 1. worker上线后写入zk的路径1
 * 2. scheduler的leader监听路径1，并且持续ping路径1中的地址
 * 3. 将ping通的worker丢进另一个zk路径2
 * 4. 全部scheduler都监听路径2，将路径2认为是可执行任务的worker
 * <p>
 * 做的事情就是，监听zk是否有新worker节点
 * WorkerClusterManager会触发WorkerTaskDefinitionManager的相关动作。
 * 注意，WorkerClusterManager所管理的节点并不一定是真的能干活的，因为他不管理每个节点的taskDefinition情况，他只看节点是否健康
 */
@Service
@Slf4j
public class WorkerClusterManager {

    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private CuratorFramework curatorZKClient;

    @Autowired
    private ZKPathProperty zkPathProperty;

    @Autowired
    private ClusterNodeLogService clusterNodeLogService;

    @Autowired
    private WorkerClusterClient workerClusterClient;

    private CuratorCache workerCuratorCache;

    // 活跃的节点，节点的上下线可能同时发生，因此需要并发安全
    private final Map<String, String> onlineWorkerPathAddress = new ConcurrentHashMap<>();
    private final Map<String, Integer> pingFailedPathCounter = new ConcurrentHashMap<>();
    private final Map<String, Integer> pingSucceedPathCounter = new ConcurrentHashMap<>();
    // 终止中的节点
    private final Map<String, String> terminatingWorkerPathAddress = new ConcurrentHashMap<>();

    // 当监听的worker的路径发生变化时，这个线程池负责实际执行监听回调，线程池设置为4，因为每个任务其实都不大，实际要参考集群的情况来控制
    private final ThreadPoolExecutor handleWorkerEventThreadPool = new ThreadPoolExecutor(4, 4, 1000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    // 确保ping是串行的，后续在ping的时候并行
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);


    /**
     * 管理worker节点
     */
    public void manageWorkers() {

        this.onlineWorkerPathAddress.clear();
        this.terminatingWorkerPathAddress.clear();

        watchOnlineWorkers();
        pingWorkers();

    }

    private void watchOnlineWorkers() {
        CuratorCacheListener listener = CuratorCacheListener.builder().forTreeCache(curatorZKClient, new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                log.debug("A Zookeeper node path event has been monitored.");
                handleWorkerEventThreadPool.submit(() -> {
                    TreeCacheEvent.Type type = event.getType();
                    switch (type) {
                        case NODE_ADDED:
                            handleAddWorker(event);
                            break;
                        case NODE_REMOVED:
                            handleRemoveWorker(event);
                            break;
                        default:
                    }
                });
            }
        }).build();


        workerCuratorCache = CuratorCache.builder(curatorZKClient, zkPathProperty.getWorker().getOnline()).build();
        workerCuratorCache.listenable().addListener(listener);
        workerCuratorCache.start();
    }

    private void handleAddWorker(TreeCacheEvent event) {
        ChildData eventData = event.getData();
        if (eventData == null) {
            return;
        }
        String workerNodeAddress = new String(eventData.getData(), StandardCharsets.UTF_8);
        String zkPath = eventData.getPath();

        String parentPath = Paths.get(eventData.getPath()).getParent().toString();
        if (StringUtils.equals(parentPath, zkPathProperty.getWorker().getOnline())) {
            // 如果是online下的节点
            log.info("new online worker, path: {}, workerNodeAddress: {}", zkPath, workerNodeAddress);
            onlineWorkerPathAddress.put(zkPath, workerNodeAddress);

            clusterNodeLogService.create(
                    new ClusterNodeLog()
                            .withNodeAddress(workerNodeAddress)
                            .withNodeType(NodeType.WORKER.getType())
                            .withNodeAction(NodeAction.RUNNABLE.getStatus()));


        } else if (StringUtils.equals(parentPath, zkPathProperty.getWorker().getTerminating())) {
            // 如果是terminating下的节点
            log.info("add terminating worker, path: {}, workerNodeAddress: {}", zkPath, workerNodeAddress);
            terminatingWorkerPathAddress.put(zkPath, workerNodeAddress);

            clusterNodeLogService.create(
                    new ClusterNodeLog()
                            .withNodeAddress(workerNodeAddress)
                            .withNodeType(NodeType.WORKER.getType())
                            .withNodeAction(NodeAction.TERMINATING.getStatus()));

        }
    }

    private void handleRemoveWorker(TreeCacheEvent event) {
        ChildData eventData = event.getData();
        if (eventData == null) {
            return;
        }

        String workerNodeAddress = new String(eventData.getData(), StandardCharsets.UTF_8);
        String zkPath = eventData.getPath();

        String parentPath = Paths.get(zkPath).getParent().toString();
        if (StringUtils.equals(parentPath, zkPathProperty.getWorker().getOnline())) {
            // 如果是online下的节点被移除了
            log.info("remove runnable worker, path: {}, workerNodeAddress: {}", zkPath, workerNodeAddress);
            onlineWorkerPathAddress.remove(zkPath);
            try {
                curatorZKClient.delete()
                        .forPath(Paths.get(zkPathProperty.getWorker().getRunnable(), Paths.get(zkPath).getFileName().toString()).toString());
            } catch (Exception e) {
                log.error("error when delete connected node", e);
            }
            clusterNodeLogService.create(
                    new ClusterNodeLog()
                            .withNodeAddress(workerNodeAddress)
                            .withNodeType(NodeType.WORKER.getType())
                            .withNodeAction(NodeAction.RUNNABLE_TO_TERMINATING.getStatus()));
        } else if (StringUtils.equals(parentPath, zkPathProperty.getWorker().getTerminating())) {
            // 如果是terminating下的节点被移除了，那就是真下线了
            log.info("remove terminating worker, path: {}, workerNodeAddress: {}", zkPath, workerNodeAddress);
            terminatingWorkerPathAddress.remove(zkPath);
            publisher.publishEvent(new WorkerTerminatedEvent(workerNodeAddress));
            clusterNodeLogService.create(
                    new ClusterNodeLog()
                            .withNodeAddress(workerNodeAddress)
                            .withNodeType(NodeType.WORKER.getType())
                            .withNodeAction(NodeAction.TERMINATED.getStatus()));
        }

    }


    // 应用关闭的时候触发
    @PreDestroy
    public void stopManageWorkers() {
        this.workerCuratorCache.close();
    }

    /**
     * 监听worker节点，worker节点的状态变化都通过zk监听实现，而不是通过请求实现，避免数据不一致
     */
    private void pingWorkers() {
        Runnable pingWorkerTask = new Runnable() {
            @Override
            public void run() {

                log.debug("ping runnable");
                Tuple2<Map<String, String>, Map<String, String>> pingRunnableResult = ping(onlineWorkerPathAddress);
                // ping失败的
                pingRunnableResult._2.forEach((path, address) -> {
                    pingSucceedPathCounter.remove(path);
                    if (pingFailedPathCounter.containsKey(path) && pingFailedPathCounter.get(path) >= 4) {
                        // 连续4次ping失败
                        try {
                            curatorZKClient.delete()
                                    .forPath(Paths.get(zkPathProperty.getWorker().getRunnable(),
                                            Paths.get(path).getFileName().toString()).toString());
                        } catch (Exception e) {
                            log.error("error when delete connected node", e);
                        }
                    } else {
                        pingFailedPathCounter.put(path, pingFailedPathCounter.getOrDefault(path, 0) + 1);
                    }
                });

                // ping成功的
                pingRunnableResult._1.forEach((path, address) -> {
                    Path runnablePath = Paths.get(zkPathProperty.getWorker().getRunnable(), Paths.get(path).getFileName().toString());
                    if (pingSucceedPathCounter.containsKey(path) && pingSucceedPathCounter.get(path) >= 4) {
                        // 连续ping4次成功，认为稳定，上线。
                        pingFailedPathCounter.remove(path);
                        try {
                            if (curatorZKClient.checkExists().forPath(runnablePath.toString()) == null) {
                                curatorZKClient.create()
                                        .withMode(CreateMode.PERSISTENT)
                                        .forPath(runnablePath.toString(), address.getBytes());
                            }
                        } catch (Exception e) {
                            log.error("error when create connected worker path", e);
                        }
                    } else {
                        pingSucceedPathCounter.put(path, pingSucceedPathCounter.getOrDefault(path, 0) + 1);
                    }

                });

                scheduledThreadPoolExecutor.schedule(this, 10, TimeUnit.SECONDS);
            }
        };
        scheduledThreadPoolExecutor.schedule(pingWorkerTask, 10, TimeUnit.SECONDS);
    }

    /**
     * ping一些节点并获取ping结果
     *
     * @param workerPathAddress 要ping的节点的路径和地址
     * @return ping的结果，是一个元组，pingResult[0]是ping成功的，pingResult[1]是ping失败的
     */
    private Tuple2<Map<String, String>, Map<String, String>> ping(Map<String, String> workerPathAddress) {
        Map<String, String> newMissingWorkerAddressPath = new HashMap<>();
        Map<String, String> newConnectedWorkerAddressPath = new HashMap<>();

        // todo: 多线程 or 多路复用改造
        for (Map.Entry<String, String> kv : workerPathAddress.entrySet()) {
            URI host = this.getWorkerURI(kv.getValue());
            try {
                HttpBody<String> ping = workerClusterClient.ping(host);
                newConnectedWorkerAddressPath.put(kv.getKey(), kv.getValue());
            } catch (Exception e) {
                log.error("error when ping active {}", kv.getValue(), e);
                newMissingWorkerAddressPath.put(kv.getKey(), kv.getValue());
            }
        }

        //
        Tuple2<Map<String, String>, Map<String, String>> pingResult = new Tuple2<>(newConnectedWorkerAddressPath,
                newMissingWorkerAddressPath);

        return pingResult;
    }


    /**
     * 获取runnable状态的worker
     * 不能直接将runnableWorkerPathId返回出去，防止外界进行操作
     *
     * @return
     */
    public Set<String> getRunnableWorkerNodeAddresses() {
        return new HashSet<>(this.onlineWorkerPathAddress.values());
    }


    public URI getWorkerURI(String workerNodeAddress) {
        return UriComponentsBuilder.fromHttpUrl("http://" + workerNodeAddress).build().toUri();
    }

}
