package org.talk.is.cheap.project.free.flow.scheduler.cluster.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.talk.is.cheap.project.free.flow.common.enums.EnvType;
import org.talk.is.cheap.project.free.flow.common.utils.IPUtil;
import org.talk.is.cheap.project.free.flow.common.enums.NodeAction;
import org.talk.is.cheap.project.free.flow.common.enums.NodeType;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNodeLog;
import org.talk.is.cheap.project.free.flow.starter.repository.service.ClusterNodeLogService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 负责管理Scheduler集群，通过zk实现：scheduler的选举
 * SchedulerClusterManager会触发WorkerClusterManager的相关动作
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
    private String cachedLeaderAddress;

    @Autowired
    private WorkerClusterManager workerClusterManager;


    @Autowired
    private ClusterNodeLogService clusterNodeLogService;

    /**
     * 监听应用启动完成事件，进行注册
     * 等同与ApplicationListener
     *
     * @param event
     */
    @EventListener(ApplicationStartedEvent.class)
    public void registryAndElection(ApplicationStartedEvent event) throws Exception {
        log.info("scheduler start registry and election");

        election();

        clusterNodeLogService.create(
                new ClusterNodeLog()
                        .withNodeAddress(getSchedulerAddress())
                        .withNodeType(NodeType.SCHEDULER.getType())
                        .withNodeType(NodeAction.RUNNABLE.getStatus()));

    }

    /**
     * 选举
     *
     * @throws Exception
     */
    private void election() throws Exception {
        if (curatorZKClient.checkExists().forPath(zkSchedulerPath) == null) {
            try {
                curatorZKClient.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(zkSchedulerPath);
            } catch (KeeperException.NodeExistsException e) {
                log.warn("zk: {} already exists", zkSchedulerPath);
            }
        }

        final String schedulerAddress = getSchedulerAddress();

        // 关键逻辑，开始竞选
        leaderLatch = new LeaderLatch(curatorZKClient, zkSchedulerElectionPath, schedulerAddress, LeaderLatch.CloseMode.NOTIFY_LEADER);
        leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
//                当前节点成为leader的时候更新，说明本节点成为了主节点
                SchedulerClusterManager.this.cachedLeaderAddress = schedulerAddress;
                log.info("{} become leader", schedulerAddress);

                // 称为leader之后开始监听管理worker
                workerClusterManager.manageWorkers();
            }

            @Override
            public void notLeader() {
                log.info("{} is no longer leader", schedulerAddress);
                workerClusterManager.stopManageWorkers();
            }
        }, SINGLE_EXECUTOR_SERVICE);

        leaderLatch.start();

    }


    public String getSchedulerAddress() {
        return (EnvType.CONTAINER == EnvType.getByName(env) ? getContainerName() : IPUtil.getMainIP()) + ":" + port;
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


    public String getLeaderAddress() throws Exception {
        if (StringUtils.isNotBlank(this.cachedLeaderAddress)) {
            return this.cachedLeaderAddress;
        }
        if (leaderLatch == null) {
            throw new RuntimeException("leaderLatch is null, The current node has not yet participated in the election.");
        }
        if (leaderLatch.getLeader() == null) {
            return null;
        }
        this.cachedLeaderAddress = leaderLatch.getLeader().getId();
        return this.cachedLeaderAddress;
    }

    public boolean isLeader() {
        return this.leaderLatch.hasLeadership();
    }


}
