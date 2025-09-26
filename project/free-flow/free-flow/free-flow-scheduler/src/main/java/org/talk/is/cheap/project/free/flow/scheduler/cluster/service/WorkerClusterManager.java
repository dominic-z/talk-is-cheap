package org.talk.is.cheap.project.free.flow.scheduler.cluster.service;

import io.vavr.Tuple2;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.client.WorkerClusterClient;
import org.talk.is.cheap.project.free.flow.common.enums.NodeStatus;
import org.talk.is.cheap.project.free.flow.common.enums.NodeType;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.event.RunnableWorkerAddEvent;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.event.WorkerTerminatedEvent;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNodeLog;
import org.talk.is.cheap.project.free.flow.starter.repository.service.ClusterNodeLogService;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 负责管理worker集群，负责：
 * 1. 执行worker的心跳监听
 * 2. 管理worker的状态，worker的状态变化的数据源均来自监听zookeeper，即scheduler只信任来自zookeeper的关于worker的的信息。
 */
@Service
@Slf4j
public class WorkerClusterManager {

    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private CuratorFramework curatorZKClient;

    @Value("${apache.zookeeper.path.worker.root}")
    private String zkRootWorkerPath;
    @Value("${apache.zookeeper.path.worker.runnable}")
    private String zkRunnableWorkerPath;

    @Value("${apache.zookeeper.path.worker.terminating}")
    private String zkTerminatingWorkerPath;

    @Autowired
    private ClusterNodeLogService clusterNodeLogService;

    @Autowired
    private WorkerClusterClient workerClusterClient;

    private CuratorCache workerCuratorCache;

    // 活跃的节点
    private final Map<String, String> runnableWorkerPathAddress = new ConcurrentHashMap<>();

    // 防止外界get runnableWorkerPathId的时候一直重复读取runnableWorkerPathId，做一个缓存，只是为了加速，不需要线程安全
    private volatile boolean runnableWorkerModified = false;
    private List<String> cachedRunnableWorkerAddresses;
    // 关闭中的节点
    private final Map<String, String> terminatingWorkerPathAddress = new ConcurrentHashMap<>();

    // ping失败但是没有下线的节点
    private final Map<String, String> missingRunnableWorkerPathAddress = new ConcurrentHashMap<>();
    private final Map<String, String> missingTerminatingWorkerPathAddress = new ConcurrentHashMap<>();


    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);


    /**
     * 管理worker节点
     */
    public void manageWorkers() {

        this.runnableWorkerPathAddress.clear();
        this.missingTerminatingWorkerPathAddress.clear();
        this.terminatingWorkerPathAddress.clear();
        this.missingTerminatingWorkerPathAddress.clear();

        watchWorkers();
        pingWorkers();

    }

    private void watchWorkers() {
        CuratorCacheListener listener = CuratorCacheListener.builder().forTreeCache(curatorZKClient, new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
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
            }
        }).build();


        workerCuratorCache = CuratorCache.builder(curatorZKClient, zkRootWorkerPath).build();
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
        if (StringUtils.equals(parentPath, zkRunnableWorkerPath)) {
            // 如果是runnable下的节点
            log.info("add runnable worker, path: {}, workerNodeAddress: {}", zkPath, workerNodeAddress);
            runnableWorkerPathAddress.put(zkPath, workerNodeAddress);
            missingRunnableWorkerPathAddress.remove(zkPath);

            runnableWorkerModified = true;

            clusterNodeLogService.create(
                    new ClusterNodeLog()
                            .withNodeAddress(workerNodeAddress)
                            .withNodeType(NodeType.WORKER.getType())
                            .withNodeStatus(NodeStatus.RUNNABLE.getStatus()));

            // 发布新增worker事件，用于触发读取worker中定义的task定义
            publisher.publishEvent(new RunnableWorkerAddEvent(workerNodeAddress));

        } else if (StringUtils.equals(parentPath, zkTerminatingWorkerPath)) {
            // 如果是terminating下的节点
            log.info("add terminating worker, path: {}, workerNodeAddress: {}", zkPath, workerNodeAddress);
            terminatingWorkerPathAddress.put(zkPath, workerNodeAddress);
            missingTerminatingWorkerPathAddress.remove(zkPath);

            clusterNodeLogService.create(
                    new ClusterNodeLog()
                            .withNodeAddress(workerNodeAddress)
                            .withNodeType(NodeType.WORKER.getType())
                            .withNodeStatus(NodeStatus.TERMINATING.getStatus()));

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
        if (StringUtils.equals(parentPath, zkRunnableWorkerPath)) {
            // 如果是runnable下的节点
            log.info("remove runnable worker, path: {}, workerNodeAddress: {}", zkPath, workerNodeAddress);
            // todo: 有并发问题，如果不加锁在处理event的时候ping操作可能并发操作这两个map，所以可能导致真正下线的worker可能还会留存在missing之中。所以只能加锁，一开始开发的时候没有发现问题，todo就记录一下
            synchronized (this) {
                runnableWorkerPathAddress.remove(zkPath);
                missingRunnableWorkerPathAddress.remove(zkPath);
            }
            runnableWorkerModified = true;
            clusterNodeLogService.create(
                    new ClusterNodeLog()
                            .withNodeAddress(workerNodeAddress)
                            .withNodeType(NodeType.WORKER.getType())
                            .withNodeStatus(NodeStatus.RUNNABLE_TERMINATING.getStatus()));
        } else if (StringUtils.equals(parentPath, zkTerminatingWorkerPath)) {
            // 如果是terminating下的节点
            log.info("remove terminating worker, path: {}, workerNodeAddress: {}", zkPath, workerNodeAddress);
            synchronized (this) {
                terminatingWorkerPathAddress.remove(zkPath);
                missingTerminatingWorkerPathAddress.remove(zkPath);
            }
            publisher.publishEvent(new WorkerTerminatedEvent(workerNodeAddress));
            clusterNodeLogService.create(
                    new ClusterNodeLog()
                            .withNodeAddress(workerNodeAddress)
                            .withNodeType(NodeType.WORKER.getType())
                            .withNodeStatus(NodeStatus.TERMINATED.getStatus()));
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
                if (ping(runnableWorkerPathAddress, missingRunnableWorkerPathAddress)) {
                    runnableWorkerModified = true;
                }

                log.debug("ping terminating");
                ping(terminatingWorkerPathAddress, missingTerminatingWorkerPathAddress);


                scheduledThreadPoolExecutor.schedule(this, 10, TimeUnit.SECONDS);
            }
        };
        ScheduledFuture<?> schedule = scheduledThreadPoolExecutor.schedule(pingWorkerTask, 10, TimeUnit.SECONDS);
    }

    /**
     * 方法的作用：
     * 1. ping connectedWorkerPathAddress中的节点，将其中ping失败的节点丢进missingWorkerPathAddress中
     * 2. ping missingWorkerPathAddress中的节点，将其中ping成功的丢进connectedWorkerPathAddress
     * @param connectedWorkerPathAddress 目前处于链接状态的节点
     * @param missingWorkerPathAddress   目前失联的节点
     * @return 是否产生了新的已连接的或者丢失的节点
     */
    private boolean ping(Map<String, String> connectedWorkerPathAddress, Map<String, String> missingWorkerPathAddress) {
        boolean modified = false;

        // ping处于连接状态的节点
        Tuple2<Map<String, String>, Map<String, String>> pingConnectedResult = ping(connectedWorkerPathAddress);
        synchronized (this) {
            Map<String, String> pingSuccess = pingConnectedResult._1;
            pingSuccess.forEach((path, address) -> {
                if (connectedWorkerPathAddress.remove(path) != null) {
                    // todo: 光加锁还不行，得这样判断一下，因为如果是节点下线导致的ping出现异常，那么这个节点一定会出现在getNewMissingResult里，如果不这样判断一下而是直接往missingWorkerPathId里面put
                    //  ，那还是会出现下线节点一直存在在missing中
                    // 如果remove返回的不是null，说明这个节点已经被其他线程remove了（目前只有节点下线一种情况）
                    missingWorkerPathAddress.put(path, address);
                }
            });
            modified = !pingConnectedResult._2.isEmpty();
        }


        // ping失联的节点
        Tuple2<Map<String, String>, Map<String, String>> pingMissingResult = ping(missingWorkerPathAddress);
        synchronized (this) {
            pingMissingResult._1.forEach((path, address) -> {
                if (missingWorkerPathAddress.remove(path) != null) {
                    connectedWorkerPathAddress.put(path, address);
                }
            });
            modified = modified || !pingConnectedResult._1.isEmpty();
        }
        log.debug("connected worker: {}, missing worker: {}", connectedWorkerPathAddress.keySet(), missingWorkerPathAddress.keySet());
        return modified;
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

//        PingWorkerResultDTO pingWorkerResultBO = new PingWorkerResultDTO();
//        pingWorkerResultBO.setConnectedResult(newConnectedWorkerAddressPath);
//        pingWorkerResultBO.setMissingResult(newMissingWorkerAddressPath);

        return pingResult;
    }


    /**
     * 获取runnable状态的worker
     * 不能直接将runnableWorkerPathId返回出去，防止外界进行操作
     *
     * @return
     */
    public Set<String> getRunnableWorkerNodeAddresses() {
        return new HashSet<>(this.runnableWorkerPathAddress.values());
    }


    public URI getWorkerURI(String workerNodeAddress) {
        return UriComponentsBuilder.fromHttpUrl("http://" + workerNodeAddress).build().toUri();
    }

}
