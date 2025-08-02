package org.talk.is.cheap.project.free.flow.scheduler.cluster.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.talk.is.cheap.project.free.flow.common.enums.EnvType;
import org.talk.is.cheap.project.free.flow.common.utils.IPUtils;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.enums.NodeStatus;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.enums.NodeType;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.pojo.ClusterNodeRegistryLog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 负责管理Scheduler集群
 */
@Component
@Slf4j
public class SchedulerClusterManager {

    private static final ExecutorService SINGLE_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @Autowired
    private CuratorFramework curatorZKClient;

    @Value("${apache.zookeeper.path.scheduler.election}")
    private String zkSchedulerElectionPath;

    @Value("${apache.zookeeper.path.scheduler.root}")
    private String zkSchedulerPath;

    @Value("${spring.application.env-type}")
    private String env;

    @Value("${server.port}")
    private String port;

    private LeaderLatch leaderLatch;

    // 用做缓存，不需要每次都去查询
    private String cachedLeaderId;

    @Autowired
    private WorkerClusterManager workerClusterManager;


    @Autowired
    private ClusterNodeRegistryLogService clusterNodeRegistryLogService;

    /**
     * 监听应用启动完成事件，进行注册
     * 等同与ApplicationListener
     *
     * @param event
     */
    @EventListener(ApplicationStartedEvent.class)
    public void registryAndElection() throws Exception {
        log.info("scheduler start registry and election");

        election();

        clusterNodeRegistryLogService.create(
                new ClusterNodeRegistryLog()
                        .withNodeId(getSchedulerId())
                        .withNodeType(NodeType.SCHEDULER.getType())
                        .withNodeStatus(NodeStatus.RUNNABLE.getStatus()));

    }

    /**
     * 选举
     *
     * @throws Exception
     */
    private void election() throws Exception {
        if (curatorZKClient.checkExists().forPath(zkSchedulerPath) == null) {
            curatorZKClient.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(zkSchedulerPath);
        }

        // 例如：注册服务到注册中心
        final String registryId = getSchedulerId();

        leaderLatch = new LeaderLatch(curatorZKClient, zkSchedulerElectionPath, registryId, LeaderLatch.CloseMode.NOTIFY_LEADER);
        leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
//                当前节点成为leader的时候更新，说明本节点成为了主节点
                SchedulerClusterManager.this.cachedLeaderId = registryId;
                log.info("{} become leader", registryId);

                // 称为leader之后开始监听管理worker
                workerClusterManager.manageWorkers();
            }

            @Override
            public void notLeader() {
                log.info("{} is no longer leader", registryId);
                workerClusterManager.stopManageWorkers();
            }
        }, SINGLE_EXECUTOR_SERVICE);

        leaderLatch.start();

    }



    public String getSchedulerId() {
        return (EnvType.CONTAINER == EnvType.getByName(env) ? getContainerName() : IPUtils.getMainIP()) + ":" + port;
    }

    /**
     * 实验环境为docker容器，需要获取容器的name用于选举，实际环境可以修改为IP地址等
     * # 启动容器时通过 -e 注入容器名称到环境变量
     * docker run -d --name my-container \
     * -e CONTAINER_NAME=my-container \
     * nginx:alpine
     *
     * @return
     */
    private String getContainerName() {
        return System.getenv("CONTAINER_NAME");
    }


    public String getLeaderId() throws Exception {
        if (StringUtils.isNotBlank(this.cachedLeaderId)) {
            return this.cachedLeaderId;
        }
        if (leaderLatch == null) {
            throw new RuntimeException("leaderLatch is null");
        }
        if (leaderLatch.getLeader() == null) {
            return null;
        }
        this.cachedLeaderId = leaderLatch.getLeader().getId();
        return leaderLatch.getLeader().getId();
    }

    public boolean isLeader() {
        return this.leaderLatch.hasLeadership();
    }


}
