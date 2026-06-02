package org.talk.is.cheap.project.free.flow.scheduler.cluster.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.Participant;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.talk.is.cheap.project.free.flow.common.enums.EnvType;
import org.talk.is.cheap.project.free.flow.common.enums.NodeStatus;
import org.talk.is.cheap.project.free.flow.common.enums.NodeType;
import org.talk.is.cheap.project.free.flow.common.utils.IPUtil;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.event.SchedulerLifecycleEvent;
import org.talk.is.cheap.project.free.flow.scheduler.config.property.ZKPathProperty;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.ClusterNodeExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNode;
import org.talk.is.cheap.project.free.flow.starter.repository.service.ClusterNodeService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.ClusterNodeServiceWrapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 负责管理Scheduler集群，通过zk实现：scheduler的选举
 * SchedulerClusterManager会触发WorkerClusterManager的相关动作
 * <p>
 * leader负责一切worker节点的管理，例如上下线
 * leader负责一切worker节点相关数据相关的操作。例如读取任务定义，写入redis（
 */
@Component
@Slf4j
public class SchedulerClusterManager {

    @Autowired
    private ApplicationEventPublisher publisher;

    private static final ExecutorService SINGLE_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @Autowired
    private CuratorFramework curatorZKClient;

    @Autowired
    private ZKPathProperty zkPathProperty;

    @Value("${spring.application.env-type}")
    private String env;

    @Value("${server.port}")
    private String port;

    private LeaderLatch leaderLatch;
    private String zkPath;

    // 用做缓存，不需要每次都去查询
    private String cachedLeaderAddress;


    private CuratorCache schedulerCuratorCache;

    private Map<String, String> addressZKPath = new ConcurrentHashMap<>();


    @Autowired
    private ClusterNodeService clusterNodeService;
    @Autowired
    private ClusterNodeServiceWrapper clusterNodeServiceWrapper;

    /**
     * 监听应用启动完成事件，进行注册
     * 等同与ApplicationListener
     * 是shceduelr的入口
     *
     * @param event
     */
    @EventListener(ApplicationStartedEvent.class)
    public void start(ApplicationStartedEvent event) throws Exception {
        log.info("scheduler start registry and election");

        register();
        election();

        publisher.publishEvent(new SchedulerLifecycleEvent(
                SchedulerLifecycleEvent.SchedulerLifeCycle.ONLINE,
                getCurrentSchedulerAddress(),
                SchedulerLifecycleEvent.Source.MYSELF
        ));

    }

    /**
     * 在zk中注册
     *
     * @throws Exception
     */
    private void register() throws Exception {
        if (curatorZKClient.checkExists().forPath(zkPathProperty.getScheduler().getRunnable()) == null) {
            try {
                curatorZKClient.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(zkPathProperty.getScheduler().getRunnable());
            } catch (KeeperException.NodeExistsException e) {
                log.warn("zk: {} already exists", zkPathProperty.getScheduler().getElection());
            }
        }


        zkPath = Paths.get(zkPathProperty.getScheduler().getRunnable(), getCurrentSchedulerAddress().replace(":", "_")).toString();
        curatorZKClient.create()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(zkPath, getCurrentSchedulerAddress().getBytes());

        watchAllSchedulers();
    }


    /**
     * 选举
     *
     * @throws Exception
     */
    private void election() throws Exception {
        if (curatorZKClient.checkExists().forPath(zkPathProperty.getScheduler().getElection()) == null) {
            try {
                curatorZKClient.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(zkPathProperty.getScheduler().getElection());
            } catch (KeeperException.NodeExistsException e) {
                log.warn("zk: {} already exists", zkPathProperty.getScheduler().getElection());
            }
        }

        final String schedulerAddress = getCurrentSchedulerAddress();

        // 关键逻辑，开始竞选

        leaderLatch = new LeaderLatch(curatorZKClient, zkPathProperty.getScheduler().getElection(), schedulerAddress,
                LeaderLatch.CloseMode.NOTIFY_LEADER);
        leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
//                当前节点成为leader的时候更新，说明本节点成为了主节点
                SchedulerClusterManager.this.cachedLeaderAddress = schedulerAddress;
                log.info("{} become leader", schedulerAddress);


                // 成为leader后更新数据库中的leader记录，scheduler节点的leader状态
                try {
                    updateLeaderInfoInDB();
                } catch (Exception e) {
                    log.error("error when updateLeaderInfoInDB", e);
                }

            }

            @Override
            public void notLeader() {
                log.info("{} is no longer leader", schedulerAddress);

                stop();
            }
        }, SINGLE_EXECUTOR_SERVICE);

        leaderLatch.start();


    }


    /**
     * 更新db中leader节点的状态，注意，这块没有开事务，没太大关系。。
     */
    private void updateLeaderInfoInDB() throws Exception {


        ClusterNodeExample example = new ClusterNodeExample();
        ClusterNodeExample.Criteria criteria = example.createCriteria();
        criteria.andNodeTypeEqualTo(NodeType.SCHEDULER_LEADER.getType());
        List<ClusterNode> oldLeaders = clusterNodeService.selectByExample(example);
        for (ClusterNode oldLeader : oldLeaders) {
            try {
                example.clear();
                example.createCriteria().andIdEqualTo(oldLeader.getId());
                if (curatorZKClient.checkExists().forPath(oldLeader.getNodeZkPath()) != null) {
                    // 如果前一轮旧的leader仍然在线上。
                    clusterNodeServiceWrapper.updateByIdSelective(oldLeader.getId(),
                            new ClusterNode().withNodeType(NodeType.SCHEDULER.getType()), null);
                } else {
                    clusterNodeServiceWrapper.updateByIdSelective(oldLeader.getId(),
                            new ClusterNode().withStatus(NodeStatus.TERMINATED.getStatus()), null);
                }
            } catch (Exception e) {
                log.error("更新旧leader异常", e);
            }
        }

        ClusterNode record = new ClusterNode()
                .withNodeAddress(getCurrentSchedulerAddress())
                .withNodeZkPath(zkPath)
                .withNodeType(NodeType.SCHEDULER_LEADER.getType())
                .withStatus(NodeStatus.RUNNABLE.getStatus());
        clusterNodeService.createOnDuplicateKey(record);

    }

    // 监听所有Scheduler的上下线，但这里有个坑，如果连续两个leader下线，在新的leader选举出来之前，如果又有节点下线，那么这个节点的下线不会触发CHILD_REMOVED
    // 因为此时没有leader，并不想一直轮询查询，所以采用懒更新的方式，每次查询的时候校验一下，如果是失效节点就更新db中的节点状态。
    // 删除leader才能监听的逻辑，这样就没问题了
    private void watchAllSchedulers() {
        try {
            List<String> schedulers = curatorZKClient.getChildren().forPath(zkPathProperty.getScheduler().getRunnable());
            for (String schedulerName : schedulers) {
                try {
                    String path = Paths.get(zkPathProperty.getScheduler().getRunnable(), schedulerName).toString();
                    String schedulerAddress = new String(curatorZKClient.getData().forPath(path), StandardCharsets.UTF_8);
                    this.addressZKPath.put(schedulerAddress, path);
                } catch (Exception e) {
                    log.error("读取scheduler的zk地址报错，跳过并继续遍历", e);
                }
            }

        } catch (Exception e) {
            log.error("获取scheduler列表报错", e);
            throw new RuntimeException(e);
        }

        CuratorCacheListener schedulerPathListener =
                CuratorCacheListener.builder().forPathChildrenCache(zkPathProperty.getScheduler().getRunnable(),
                        this.curatorZKClient,
                        new PathChildrenCacheListener() {
                            @Override
                            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                                log.debug("listen schedulers, eventType:{}, eventPath:{}", event.getType(),
                                        event.getData() == null ? "nullData" : event.getData().getPath());
                                if (event.getData() == null || event.getData().getData() == null ||
                                        StringUtils.equals(event.getData().getPath(), zkPath)) {
                                    // 如果事件没有数据，或者事件就是本节点的新增事件，那么就不做处理。
                                    return;
                                }
                                String nodeAddress = new String(event.getData().getData());
                                if (PathChildrenCacheEvent.Type.CHILD_ADDED.equals(event.getType())) {
                                    // 开始监听之前已经存在的数据也会触发CHILD_ADD事件
                                    ClusterNode clusterNode = new ClusterNode().withNodeAddress(nodeAddress)
                                            .withNodeZkPath(event.getData().getPath())
                                            .withStatus(NodeStatus.RUNNABLE.getStatus())
                                            .withNodeType(NodeType.SCHEDULER.getType());
                                    clusterNodeService.createOnDuplicateKey(clusterNode);
                                    addressZKPath.put(nodeAddress, event.getData().getPath());
                                    publisher.publishEvent(
                                            new SchedulerLifecycleEvent(
                                                    SchedulerLifecycleEvent.SchedulerLifeCycle.ONLINE, nodeAddress,
                                                    SchedulerLifecycleEvent.Source.ZOOKEEPER));
                                } else if (PathChildrenCacheEvent.Type.CHILD_REMOVED.equals(event.getType())) {
                                    ClusterNode clusterNode = new ClusterNode().withNodeAddress(nodeAddress)
                                            .withStatus(NodeStatus.TERMINATED.getStatus())
                                            .withNodeType(NodeType.SCHEDULER.getType());
                                    clusterNodeServiceWrapper.updateByNodeAddressSelective(nodeAddress, clusterNode);
                                    addressZKPath.remove(nodeAddress);
                                    publisher.publishEvent(
                                            new SchedulerLifecycleEvent(
                                                    SchedulerLifecycleEvent.SchedulerLifeCycle.OFFLINE, nodeAddress,
                                                    SchedulerLifecycleEvent.Source.ZOOKEEPER));
                                }
                            }
                        }).build();

        schedulerCuratorCache = CuratorCache.builder(curatorZKClient, zkPathProperty.getScheduler().getRunnable()).build();
        schedulerCuratorCache.listenable().addListener(schedulerPathListener);

        schedulerCuratorCache.start();
    }

    @PreDestroy
    public void stop() {
        publisher.publishEvent(
                new SchedulerLifecycleEvent(
                        SchedulerLifecycleEvent.SchedulerLifeCycle.OFFLINE,
                        getCurrentSchedulerAddress(),
                        SchedulerLifecycleEvent.Source.MYSELF));
        if (schedulerCuratorCache != null) {
            schedulerCuratorCache.close();
        }
    }

    public String getCurrentSchedulerAddress() {
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

    public boolean isLeader(String nodeAddress) {
        return StringUtils.equals(nodeAddress, getLeaderId());
    }

    public String getLeaderId() {
        try {
            String id = this.leaderLatch.getLeader().getId();
            return id;
        } catch (Exception e) {
            log.error("获取leader异常", e);
            return null;
        }
    }

    public boolean isValid(String zkPath) {
        try {
            return curatorZKClient.checkExists().forPath(zkPath) == null;
        } catch (Exception e) {
            log.error("检查zkPath:{}是否存在出错", zkPath, e);
            return false;
        }
    }

    public Set<String> getSchedulerAddresses() {
        return new HashSet<>(this.addressZKPath.keySet());
    }

}
