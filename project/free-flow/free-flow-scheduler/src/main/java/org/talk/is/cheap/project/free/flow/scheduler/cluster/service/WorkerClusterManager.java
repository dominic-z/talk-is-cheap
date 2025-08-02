package org.talk.is.cheap.project.free.flow.scheduler.cluster.service;

import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.client.WorkerClusterClient;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.enums.NodeStatus;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.enums.NodeType;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.pojo.ClusterNodeRegistryLog;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 负责管理worker集群
 */
@Service
@Slf4j
public class WorkerClusterManager {

    @Data
    private static class PingWorkerResultBO {
        private Map<String, String> newMissingResult;
        private Map<String, String> newConnectedWorkerId;
    }


    @Autowired
    private CuratorFramework curatorZKClient;

    @Value("${apache.zookeeper.path.worker.root}")
    private String zkRootWorkerPath;
    @Value("${apache.zookeeper.path.worker.runnable}")
    private String zkRunnableWorkerPath;

    @Value("${apache.zookeeper.path.worker.terminating}")
    private String zkTerminatingWorkerPath;

    @Autowired
    private ClusterNodeRegistryLogService clusterNodeRegistryLogService;

    @Autowired
    private WorkerClusterClient workerClusterClient;

    private CuratorCache workerCuratorCache;

    // 活跃的节点
    private final Map<String, String> runnableWorkerPathId = new ConcurrentHashMap<>();
    // 关闭中的节点
    private final Map<String, String> terminatingWorkerPathId = new ConcurrentHashMap<>();

    // ping失败但是没有下线的节点
    private final Map<String, String> missingRunnableWorkerPathId = new ConcurrentHashMap<>();
    private final Map<String, String> missingTerminatingWorkerPathId = new ConcurrentHashMap<>();


    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);


    /**
     * 管理worker节点
     */
    public void manageWorkers() {

        this.runnableWorkerPathId.clear();
        this.missingTerminatingWorkerPathId.clear();
        this.terminatingWorkerPathId.clear();
        this.missingTerminatingWorkerPathId.clear();

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
        String workerId = new String(eventData.getData(), StandardCharsets.UTF_8);
        String zkPath = eventData.getPath();

        String parentPath = Paths.get(eventData.getPath()).getParent().toString();
        if (StringUtils.equals(parentPath, zkRunnableWorkerPath)) {
            // 如果是runnable下的节点
            log.info("add runnable worker, path: {}, workerId: {}", zkPath, workerId);
            runnableWorkerPathId.put(zkPath, workerId);
            missingRunnableWorkerPathId.remove(zkPath);

            clusterNodeRegistryLogService.create(new ClusterNodeRegistryLog().withNodeId(workerId).withNodeType(NodeType.WORKER.getType()).withNodeStatus(NodeStatus.RUNNABLE.getStatus()));

        } else if (StringUtils.equals(parentPath, zkTerminatingWorkerPath)) {
            // 如果是terminating下的节点
            log.info("add terminating worker, path: {}, workerId: {}", zkPath, workerId);
            terminatingWorkerPathId.put(zkPath, workerId);
            missingTerminatingWorkerPathId.remove(zkPath);

            clusterNodeRegistryLogService.create(new ClusterNodeRegistryLog().withNodeId(workerId).withNodeType(NodeType.WORKER.getType()).withNodeStatus(NodeStatus.TERMINATING.getStatus()));

        }
    }

    private void handleRemoveWorker(TreeCacheEvent event) {
        ChildData eventData = event.getData();
        if (eventData == null) {
            return;
        }

        String workerId = new String(eventData.getData(), StandardCharsets.UTF_8);
        String zkPath = eventData.getPath();

        String parentPath = Paths.get(zkPath).getParent().toString();
        if (StringUtils.equals(parentPath, zkRunnableWorkerPath)) {
            // 如果是runnable下的节点
            log.info("remove runnable worker, path: {}, workerId: {}", zkPath, workerId);
            // todo: 有并发问题，如果不加锁在处理event的时候ping操作可能并发操作这两个map，所以可能导致真正下线的worker可能还会留存在missing之中。所以只能加锁，一开始开发的时候没有发现问题，todo就记录一下
            synchronized (this) {
                runnableWorkerPathId.remove(zkPath);
                missingRunnableWorkerPathId.remove(zkPath);
            }

            clusterNodeRegistryLogService.create(new ClusterNodeRegistryLog().withNodeId(workerId).withNodeType(NodeType.WORKER.getType()).withNodeStatus(NodeStatus.QUIT_RUNNABLE.getStatus()));
        } else if (StringUtils.equals(parentPath, zkTerminatingWorkerPath)) {
            // 如果是terminating下的节点
            log.info("remove terminating worker, path: {}, workerId: {}", zkPath, workerId);
            synchronized (this) {
                terminatingWorkerPathId.remove(zkPath);
                missingTerminatingWorkerPathId.remove(zkPath);
            }
            clusterNodeRegistryLogService.create(new ClusterNodeRegistryLog().withNodeId(workerId).withNodeType(NodeType.WORKER.getType()).withNodeStatus(NodeStatus.TERMINATED.getStatus()));
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

                log.info("ping runnable");
                ping(runnableWorkerPathId, missingRunnableWorkerPathId);

                log.info("ping terminating");
                ping(terminatingWorkerPathId, missingTerminatingWorkerPathId);


                scheduledThreadPoolExecutor.schedule(this, 10, TimeUnit.SECONDS);
            }
        };
        ScheduledFuture<?> schedule = scheduledThreadPoolExecutor.schedule(pingWorkerTask, 10, TimeUnit.SECONDS);
    }

    /**
     * @param connectedWorkerPathId 目前处于链接状态的节点
     * @param missingWorkerPathId   目前失联的节点
     */
    private void ping(Map<String, String> connectedWorkerPathId, Map<String, String> missingWorkerPathId) {
        // ping连接状态的节点
        PingWorkerResultBO pingRunnableResult = ping(connectedWorkerPathId);
        synchronized (this) {
            pingRunnableResult.getNewMissingResult().forEach((path, id) -> {
                if(connectedWorkerPathId.remove(path)!=null){
                    // todo: 光加锁还不行，得这样判断一下，因为如果是节点下线导致的ping出现异常，那么这个节点一定会出现在getNewMissingResult里，如果不这样判断一下而是直接往missingWorkerPathId里面put，那还是会出现下线节点一直存在在missing中
                    // 如果remove返回的不是null，说明这个节点已经被其他线程remove了（目前只有节点下线一种情况）
                    missingWorkerPathId.put(path, id);
                }
            });
        }


        // ping失联的节点
        PingWorkerResultBO pingMissingRunnableResult = ping(missingWorkerPathId);
        synchronized (this) {
            pingMissingRunnableResult.getNewConnectedWorkerId().forEach((path, id) -> {
                if(missingWorkerPathId.remove(path)!=null){
                    connectedWorkerPathId.put(path, id);
                }
            });
        }
        log.info("connected worker: {}, missing worker: {}", connectedWorkerPathId.keySet(), missingWorkerPathId.keySet());
    }

    /**
     * ping一些节点并获取ping结果
     *
     * @param workerPathId 要ping的节点的路径和id
     * @return
     */
    private PingWorkerResultBO ping(Map<String, String> workerPathId) {
        Map<String, String> newMissingWorkIdPath = new HashMap<>();
        Map<String, String> newConnectedWorkerIdPath = new HashMap<>();

        // todo: 多线程
        for (Map.Entry<String, String> kv : workerPathId.entrySet()) {
            URI host = UriComponentsBuilder.fromHttpUrl("http://" + kv.getValue()).build().toUri();
            try {
                HttpBody<String> ping = workerClusterClient.ping(host);
                newConnectedWorkerIdPath.put(kv.getKey(), kv.getValue());
            } catch (Exception e) {
                log.error("error when ping active {}", kv.getValue(), e);
                newMissingWorkIdPath.put(kv.getKey(), kv.getValue());
            }
        }

        PingWorkerResultBO pingWorkerResultBO = new PingWorkerResultBO();
        pingWorkerResultBO.setNewConnectedWorkerId(newConnectedWorkerIdPath);
        pingWorkerResultBO.setNewMissingResult(newMissingWorkIdPath);

        return pingWorkerResultBO;
    }


    /**
     * 获取runnable状态的worker
     *
     * @return
     */
    public Map<String, String> getRunnableWorkers() {
        return new HashMap<>(this.runnableWorkerPathId);
    }
}
