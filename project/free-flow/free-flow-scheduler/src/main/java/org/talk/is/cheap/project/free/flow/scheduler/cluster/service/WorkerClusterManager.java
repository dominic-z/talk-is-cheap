package org.talk.is.cheap.project.free.flow.scheduler.cluster.service;

import lombok.Getter;
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
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.enums.NodeAction;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.enums.NodeType;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.pojo.ClusterNodeRegistryLog;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.query.example.ClusterNodeRegistryLogExample;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    @Getter
    private Set<String> activeWorkerIds = Collections.synchronizedSet(new HashSet<>());

    // ping失败的节点
    private Set<String> missingWorkerIds = Collections.synchronizedSet(new HashSet<>());

    // 所有worker节点的id->zkPath
    private Map<String, String> workerIdZKPath = new ConcurrentHashMap<>();


    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);


    /**
     * 管理worker节点
     */
    public void manageWorkers() {

        watchWorkers();
        pingWorkers();

    }

    private void pingWorkers() {
        Runnable pingWorkerTask = new Runnable() {
            @Override
            public void run() {
                Set<String> newMissingWorkIds = new HashSet<>();
                for (String workerId : activeWorkerIds) {
                    URI host = UriComponentsBuilder.fromHttpUrl("http://" + workerId).build().toUri();
                    try {
                        HttpBody<String> ping = workerClusterClient.ping(host);
                    } catch (Exception e) {
                        log.error("error when ping active {}", workerId, e);
                        newMissingWorkIds.add(workerId);
                    }
                }
                activeWorkerIds.removeAll(newMissingWorkIds);
                missingWorkerIds.addAll(newMissingWorkIds);

                Set<String> newActiveWorkerIds = new HashSet<>();
                for (String workerId : missingWorkerIds) {
                    URI host = UriComponentsBuilder.fromHttpUrl("http://" + workerId).build().toUri();
                    try {
                        HttpBody<String> ping = workerClusterClient.ping(host);
                        newActiveWorkerIds.add(workerId);
                    } catch (Exception e) {
                        log.error("error when ping missing {}", workerId, e);
                    }
                }
                missingWorkerIds.removeAll(newActiveWorkerIds);
                activeWorkerIds.addAll(newActiveWorkerIds);

                log.info("active worker: {}, missing worker: {}", activeWorkerIds, missingWorkerIds);
                scheduledThreadPoolExecutor.schedule(this, 10, TimeUnit.SECONDS);
            }
        };
        ScheduledFuture<?> schedule = scheduledThreadPoolExecutor.schedule(pingWorkerTask, 10, TimeUnit.SECONDS);
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

        // todo: 没有关闭
        workerCuratorCache = CuratorCache.builder(curatorZKClient, zkRootWorkerPath).build();
        workerCuratorCache.listenable().addListener(listener);
        workerCuratorCache.start();
    }

    private void handleAddWorker(TreeCacheEvent event) {
        ChildData eventData = event.getData();
        if(eventData ==null){
            return;
        }
        if (!StringUtils.equals(eventData.getPath(), zkRunnableWorkerPath)) {
            // 只监控zkWorkerPath下的节点，对zkWorkerPath不关注
            String workerId = new String(eventData.getData(), StandardCharsets.UTF_8);
            String zkPath = eventData.getPath();
            log.info("add worker, path: {}, workerId: {}", zkPath, workerId);
            URI host = UriComponentsBuilder.fromHttpUrl("http://" + workerId).build().toUri();

            try {
                HttpBody<String> ping = workerClusterClient.ping(host);
                ClusterNodeRegistryLog clusterNodeRegistryLog = new ClusterNodeRegistryLog()
                        .withNodeId(workerId)
                        .withNodeType(NodeType.WORKER.getType())
                        .withNodeAction(NodeAction.UP.getAction());
                clusterNodeRegistryLogService.create(clusterNodeRegistryLog);
            } catch (Exception e) {
                log.error("error when log worker: {}", workerId, e);
            }

            workerIdZKPath.put(workerId, zkPath);
            missingWorkerIds.remove(workerId);
            activeWorkerIds.add(workerId);
        }
    }

    private void handleRemoveWorker(TreeCacheEvent event) {
        ChildData eventData = event.getData();
        if(eventData ==null){
            return;
        }
        if (!StringUtils.equals(eventData.getPath(), zkRunnableWorkerPath)) {


            log.info("delete worker, path: {}, workerId: {}", eventData.getPath(), new String(eventData.getData(),
                    StandardCharsets.UTF_8));
            String workerId = new String(eventData.getData(), StandardCharsets.UTF_8);
            activeWorkerIds.remove(workerId);
            missingWorkerIds.remove(workerId);
            workerIdZKPath.remove(workerId);

            ClusterNodeRegistryLogExample clusterNodeRegistryLogExample = new ClusterNodeRegistryLogExample();
            clusterNodeRegistryLogExample.createCriteria()
                    .andNodeIdEqualTo(workerId)
                    .andNodeTypeEqualTo(NodeType.WORKER.getType());
            int updateCount =
                    clusterNodeRegistryLogService.updateByExampleSelective(new ClusterNodeRegistryLog().withNodeAction(NodeAction.DOWN.getAction()),
                            clusterNodeRegistryLogExample);

            if (updateCount == 0) {
                log.error("remove {} fail", workerId);
            }
        }
    }

}
