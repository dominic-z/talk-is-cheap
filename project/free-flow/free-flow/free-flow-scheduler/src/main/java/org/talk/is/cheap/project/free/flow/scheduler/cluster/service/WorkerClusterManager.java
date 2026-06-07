package org.talk.is.cheap.project.free.flow.scheduler.cluster.service;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.google.common.hash.Hashing;
import io.vavr.Tuple2;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.talk.is.cheap.project.free.flow.common.enums.NodeStatus;
import org.talk.is.cheap.project.free.flow.common.enums.NodeType;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.client.WorkerNodeClient;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.event.SchedulerLifecycleEvent;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.event.WatchWorkerTaskDefinitionEvent;
import org.talk.is.cheap.project.free.flow.scheduler.config.property.ZKPathProperty;
import org.talk.is.cheap.project.free.flow.scheduler.utils.VNConsistentHash;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.ClusterNodeExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNode;
import org.talk.is.cheap.project.free.flow.starter.repository.service.ClusterNodeService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.ClusterNodeServiceWrapper;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
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
    private SchedulerClusterManager schedulerClusterManager;

    @Autowired
    private CuratorFramework curatorZKClient;

    @Autowired
    private ZKPathProperty zkPathProperty;

    @Autowired
    private ClusterNodeService clusterNodeService;
    @Autowired
    private ClusterNodeServiceWrapper clusterNodeServiceWrapper;

    @Autowired
    private WorkerNodeClient workerNodeClient;


    private CuratorCache onlineWorkerCuratorCache;
    private CuratorCache runnableWorkerCuratorCache;

    // 活跃的节点，节点的上下线可能同时发生，因此需要并发安全
    private final Map<String, String> onlineWorkerPathAddress = new ConcurrentHashMap<>();
    private final Map<String, String> runnableWorkerPathAddress = new ConcurrentHashMap<>();
    private final Map<String, String> runnableWorkerAddressPath = new ConcurrentHashMap<>();
    private final Map<String, Integer> pingFailedAddrCounter = new ConcurrentHashMap<>();
    private final Map<String, Integer> pingSucceedAddrCounter = new ConcurrentHashMap<>();

    // 当监听的worker的路径发生变化时，这个线程池负责实际执行监听回调，线程池设置为4，因为每个任务其实都不大，实际要参考集群的情况来控制
    private final ThreadPoolExecutor handleWorkerEventThreadPool = new ThreadPoolExecutor(4, 4, 1000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    // 确保ping是串行的，后续在ping的时候并行
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

    /**
     * 管理worker节点
     */
    @EventListener(SchedulerLifecycleEvent.class)
    public void start(SchedulerLifecycleEvent event) {
        if (SchedulerLifecycleEvent.Source.MYSELF == event.getSource() && SchedulerLifecycleEvent.SchedulerLifeCycle.ONLINE == event.getLifeCycle()) {
            this.onlineWorkerPathAddress.clear();

            initVnConsistentHash();
            watchOnlineWorkers();
            watchRunnableWorkers();
            pingWorkers();

//        使用事件，避免循环依赖
            publisher.publishEvent(new WatchWorkerTaskDefinitionEvent(true, new ArrayList<>()));
        }
    }

    private void watchOnlineWorkers() {
        CuratorCacheListener listener = CuratorCacheListener.builder().forPathChildrenCache(zkPathProperty.getWorker().getOnline(),
                curatorZKClient, new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                        log.debug("A Zookeeper node path event has been monitored.");
                        CompletableFuture.runAsync(() -> {
                                    PathChildrenCacheEvent.Type type = event.getType();
                                    switch (type) {
                                        case CHILD_ADDED:
                                            onAddOnlineWorker(event);
                                            break;
                                        case CHILD_REMOVED:
                                            onRemoveOnlineWorker(event);
                                            break;
                                        default:
                                    }
                                }, handleWorkerEventThreadPool)
                                .exceptionally(e -> {
                                    log.error("处理{}路径的监听事件报错", zkPathProperty.getWorker().getOnline(), e);
                                    return null;
                                });
                    }
                }).build();


        onlineWorkerCuratorCache = CuratorCache.builder(curatorZKClient, zkPathProperty.getWorker().getOnline()).build();
        onlineWorkerCuratorCache.listenable().addListener(listener);
        onlineWorkerCuratorCache.start();
    }

    private void onAddOnlineWorker(PathChildrenCacheEvent event) {
        ChildData eventData = event.getData();
        if (eventData == null) {
            log.warn("handle add worker without data,{}", event);
            return;
        }
        String workerNodeAddress = new String(eventData.getData(), StandardCharsets.UTF_8);
        String zkPath = eventData.getPath();

        log.info("new online worker, path: {}, workerNodeAddress: {}", zkPath, workerNodeAddress);
        onlineWorkerPathAddress.put(zkPath, workerNodeAddress);

        if (schedulerClusterManager.isLeader()) {
            // leader操作数据库
            clusterNodeService.createOnDuplicateKey(
                    new ClusterNode()
                            .withStatus(NodeStatus.INITIALIZING.getStatus())
                            .withNodeZkPath(zkPath)
                            .withNodeAddress(workerNodeAddress)
                            .withNodeType(NodeType.WORKER.getType())
            );
        }

        this.assignWorkerToScheduler(workerNodeAddress);
    }

    // 如果online下线，那么说明这个节点真的已经下线而来
    private void onRemoveOnlineWorker(PathChildrenCacheEvent event) {
        ChildData eventData = event.getData();
        if (eventData == null) {
            return;
        }

        String workerNodeAddress = new String(eventData.getData(), StandardCharsets.UTF_8);
        String zkPath = eventData.getPath();

//        String parentPath = Paths.get(zkPath).getParent().toString();
//        if (StringUtils.equals(parentPath, zkPathProperty.getWorker().getOnline())) {
        // 如果是online下的节点被移除了
        // 因为监听事件并不是串行的，即使发送是串行的，到达也可能不是串行的。因此需要考虑一些节点反复上下线导致的并发异常，例如一个节点上线->下线->上线，如果下线事件反而最后处理，那么这个节点可能就丢了。
        // 可以借鉴“延迟双删”的策略，加一个延迟事件处理队列，在x秒之后再读取一下这个节点，如果在线的话，就尝试put，如果不在线就删除，当然无法完全解决问题，但是能够降低概率。
        // 更新，zk的事件监听是严格按照顺序的
        log.info("remove online worker, path: {}, workerNodeAddress: {}", zkPath, workerNodeAddress);
        onlineWorkerPathAddress.remove(zkPath);
        pingSucceedAddrCounter.remove(workerNodeAddress);
        pingFailedAddrCounter.remove(workerNodeAddress);

        if (schedulerClusterManager.isLeader()) {
            ClusterNodeExample example = new ClusterNodeExample();
            example.createCriteria().andNodeAddressEqualTo(workerNodeAddress);
            clusterNodeService.updateByExampleSelective(
                    new ClusterNode()
                            .withNodeAddress(workerNodeAddress)
                            .withStatus(NodeStatus.TERMINATED.getStatus()), example);
        }

        this.removeOnlineWorkerFromScheduler(workerNodeAddress);

    }


    // 应用关闭的时候触发
    @PreDestroy
    public void stop() {
        if (this.onlineWorkerCuratorCache != null) {
            this.onlineWorkerCuratorCache.close();
        }
        if (this.runnableWorkerCuratorCache != null) {
            this.runnableWorkerCuratorCache.close();
        }
    }

    /**
     * 监听worker节点，worker节点的状态变化都通过zk监听实现，而不是通过请求实现，避免数据不一致
     * worker自行online，然后leader ping成功一定次数之后，worker进入runnable路径，意思为可以作为一个健康的节点开始干活了
     */
    private void pingWorkers() {
        final int threshold = 1;
        Runnable pingWorkerTask = new Runnable() {
            @Override
            public void run() {

                log.debug("ping runnable");
                try {
                    Set<String> managedWorkerAddrs = getManagedWorkerAddrs();
                    if (managedWorkerAddrs != null) {
                        Tuple2<Set<String>, Set<String>> pingOnlineWorkersResults = ping(managedWorkerAddrs); // 只ping自己的
                        // ping失败的
                        pingOnlineWorkersResults._2.forEach((address) -> {
                            pingSucceedAddrCounter.remove(address);
                            if (pingFailedAddrCounter.getOrDefault(address, 0) < threshold) {
                                // 当且仅当第一次大于threshold的时候执行delete避免多次执行delete，当且仅当小于threshold的时候，计数+1
                                if (pingFailedAddrCounter.getOrDefault(address, 0) + 1 >= threshold) {
                                    // 连续x次ping失败
                                    try {
                                        String runnablePath = runnableWorkerAddressPath.get(address);
                                        if (runnablePath != null) {
                                            curatorZKClient.delete()
                                                    .forPath(Paths.get(zkPathProperty.getWorker().getRunnable(),
                                                            Paths.get(runnablePath).getFileName().toString()).toString());
                                        }
                                    } catch (Exception e) {
                                        log.error("error when delete connected node", e);
                                    }
                                }
                                pingFailedAddrCounter.put(address, pingFailedAddrCounter.getOrDefault(address, 0) + 1);
                            }
                        });

                        // ping成功的
                        pingOnlineWorkersResults._1.forEach((address) -> {

                            pingFailedAddrCounter.remove(address);
                            if (pingSucceedAddrCounter.getOrDefault(address, 0) < threshold) {
                                if (pingSucceedAddrCounter.getOrDefault(address, 0) + 1 >= threshold) {
                                    // 连续pingx次成功，认为稳定，上线。这种机制也有助于防止网络不稳定导致节点反复上下线导致的反复io，确保这个if判断只进入一次
                                    try {
                                        HttpBody<String> allowToRunResp = workerNodeClient.allowToRun(getWorkerURI(address), "null",
                                                "null");
                                        clusterNodeService.createOnDuplicateKey(
                                                new ClusterNode()
                                                        .withStatus(NodeStatus.RUNNABLE.getStatus())
                                                        .withNodeZkPath(allowToRunResp.getData())
                                                        .withNodeAddress(address)
                                                        .withNodeType(NodeType.WORKER.getType())
                                        );

                                        // 这里需要单独publish一个事件，因为有可能，worker节点已经在runnable了，然后scheduler节点都挂了
                                        // 第一个scheduler节点上线后，会先触发监听runnable节点事件，但是此时节点的任务分配还没有完成
                                        // 导致scheduler节点会忽略现有的runnable节点，这些runnable节点分配完成之后，可能之前忽略的runnable节点应该是本节点管理的
                                        // 因此发布一个这个事件，用来触发WorkerTaskDefinitionManager的onAddRunnableWorkerEvent
                                        // 方法，但实际不触发也没啥。。。因为既然在runnable了，所有的信息其实应该都解析完成了
                                        // 测试：启动一个scheduler节点，然后启动两个worker节点，等这两个worker节点runnable之后，scheduler节点下线，然后再上线，会出现“不是自己管理的节点，跳过”的日志
                                        // 这个就是因为监听runnable先于节点分配完成的
                                        publisher.publishEvent(new WatchWorkerTaskDefinitionEvent(false, List.of(address)));
                                    } catch (Exception e) {
                                        log.error("error when create connected worker path", e);
                                    }
                                }
                                pingSucceedAddrCounter.put(address, pingSucceedAddrCounter.getOrDefault(address, 0) + 1);
                            }
                        });
                    }
                } catch (Exception e) {
                    log.error("ping 过程出现异常", e);
                }


                scheduledThreadPoolExecutor.schedule(this, 10, TimeUnit.SECONDS);
            }
        };
        scheduledThreadPoolExecutor.schedule(pingWorkerTask, 10, TimeUnit.SECONDS);
    }

    /**
     * 上面的online节点的监听都是leader节点执行的，但是非leader节点也得能监控到runnable的节点，用来给其他组件提供信息
     */
    public void watchRunnableWorkers() {
        try {
            List<String> runnableWorkers = curatorZKClient.getChildren().forPath(zkPathProperty.getWorker().getRunnable());
            for (String runnableWorker : runnableWorkers) {
                String path = Paths.get(zkPathProperty.getWorker().getRunnable(), runnableWorker).toString();

                try {
                    String workerAddress = new String(curatorZKClient.getData().forPath(path));
                    WorkerClusterManager.this.runnableWorkerPathAddress.put(path, workerAddress);
                    WorkerClusterManager.this.runnableWorkerAddressPath.put(workerAddress, path);
                } catch (Exception e) {
                    log.error("首次读取runnable节点失败，跳过并继续遍历", e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        runnableWorkerCuratorCache = CuratorCache.builder(curatorZKClient, zkPathProperty.getWorker().getRunnable()).build();
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forPathChildrenCache(zkPathProperty.getWorker().getRunnable(), curatorZKClient, new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                        try {
                            if (event.getData() == null) {
                                log.info("用来监听所有的runnable路径，on runnable worker event: {}", event.getType());
                            } else {
                                String workerAddress = new String(event.getData().getData());
                                String path = event.getData().getPath();
                                log.info("用来监听所有的runnable路径，on runnable worker event: {}, zkPath: {}, address: {}", event.getType(), path,
                                        workerAddress);
                                switch (event.getType()) {
                                    case CHILD_ADDED:
                                        WorkerClusterManager.this.runnableWorkerPathAddress.put(path, workerAddress);
                                        WorkerClusterManager.this.runnableWorkerAddressPath.put(workerAddress, path);
                                        break;
                                    case CHILD_REMOVED:
                                        WorkerClusterManager.this.runnableWorkerPathAddress.remove(path);
                                        WorkerClusterManager.this.runnableWorkerAddressPath.remove(workerAddress);
                                        break;
                                }
                            }
                        } catch (Exception e) {
                            log.error("处理{}路径的监听事件异常", zkPathProperty.getWorker().getRunnable(), e);
                        }
                    }
                }).build();

        runnableWorkerCuratorCache.listenable().addListener(listener);
        runnableWorkerCuratorCache.start();
    }

    /**
     * ping一些节点并获取ping结果
     *
     * @param workerAddrs 要ping的节点的路径和地址
     * @return ping的结果，是一个元组，pingResult[0]是ping成功的，pingResult[1]是ping失败的
     */
    private Tuple2<Set<String>, Set<String>> ping(Set<String> workerAddrs) {
        Set<String> newMissingWorkerAddrs = new HashSet<>();
        Set<String> newConnectedWorkerAddrs = new HashSet<>();

        for (String addr : workerAddrs) {
            URI host = getWorkerURI(addr);
            try {
                HttpBody<String> ping = workerNodeClient.ping(host);
                newConnectedWorkerAddrs.add(addr);
            } catch (Exception e) {
                log.error("error when ping active {}", addr, e);
                newMissingWorkerAddrs.add(addr);
            }
        }

        //
        Tuple2<Set<String>, Set<String>> pingResult = new Tuple2<>(newConnectedWorkerAddrs,
                newMissingWorkerAddrs);

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

    public static URI getWorkerURI(String workerNodeAddress) {
        return UriComponentsBuilder.fromHttpUrl("http://" + workerNodeAddress).build().toUri();
    }

    public boolean isValidRunnableWorker(String workerAddr) {
        return this.runnableWorkerAddressPath.containsKey(workerAddr);
    }


    public void tryTerminate(String workerAddress) {
        ClusterNode clusterNode = clusterNodeServiceWrapper.selectByAddress(workerAddress, NodeStatus.RUNNABLE.getStatus());
        VerifyUtil.requireNotNull(clusterNode, String.format("不存在地址为%s的worker节点", workerAddress));

//        VerifyUtil.requireTrue(this.onlineWorkerPathAddress.containsKey(clusterNode.getNodeZkPath())
//                        && StringUtils.equals(workerAddress,this.onlineWorkerPathAddress.get(clusterNode.getNodeZkPath())),
//                String.format("不存在地址为%s的worker节点", workerAddress));

        try {
            String workerAddressInZK = new String(curatorZKClient.getData().forPath(clusterNode.getNodeZkPath()));
            VerifyUtil.requireEqual(workerAddress, workerAddressInZK,
                    String.format("%s在数据库中记录的zookeeper路径的节点地址为%s", workerAddress, workerAddressInZK));
        } catch (Exception e) {
            log.error("向zookeeper查询{}的数据错误", workerAddress, e);
            throw new RuntimeException(e);
        }

        HttpBody<String> terminateResp = workerNodeClient.tryTerminate(getWorkerURI(workerAddress));
        VerifyUtil.requireTrue(terminateResp.isSuccess(), terminateResp.getMsg());

        ClusterNodeExample clusterNodeExample = new ClusterNodeExample();
        clusterNodeExample.createCriteria().andNodeAddressEqualTo(workerAddress);

        clusterNodeService.updateByExampleSelective(new ClusterNode().withStatus(NodeStatus.TERMINATING.getStatus()), clusterNodeExample);
    }

    public void safeToTerminate(String workerAddress) {
        ClusterNodeExample clusterNodeExample = new ClusterNodeExample();
        clusterNodeExample.createCriteria().andNodeAddressEqualTo(workerAddress);
        clusterNodeService.updateByExampleSelective(new ClusterNode().withStatus(NodeStatus.SAFE_TO_TERMINATE.getStatus()),
                clusterNodeExample);
    }


    private final ConcurrentHashMap<String, Set<String>> schedulerDispatchedWorkers = new ConcurrentHashMap<>();
    private final VNConsistentHash vnConsistentHash = new VNConsistentHash(200);
    private final CountDownLatch vnConsistentHashInit = new CountDownLatch(1);

    private void initVnConsistentHash() {
        // 执行过程中，不能与Scheduler的上下线并发
        for (String schedulerAddress : schedulerClusterManager.getSchedulerAddresses()) {
            vnConsistentHash.addNode(schedulerAddress);
        }
        vnConsistentHashInit.countDown();
    }

    private Set<String> getManagedWorkerAddrs() {
        return schedulerDispatchedWorkers.get(schedulerClusterManager.getCurrentSchedulerAddress());
    }

    public boolean isManagedWorkerAddrs(String workerAddr) {
        String currentSchedulerAddress = schedulerClusterManager.getCurrentSchedulerAddress();
        if (schedulerDispatchedWorkers.containsKey(currentSchedulerAddress)) {
            return schedulerDispatchedWorkers.get(currentSchedulerAddress).contains(workerAddr);
        }
        return false;
    }

    private void assignWorkerToScheduler(String workerAddr) {
        // 执行过程中，不能与Scheduler的上下线并发，因此使用this作为锁就可以，下面的onNewScheduler方法在另一个线程执行
        synchronized (this) {
            String schedulerAddr = getAssignedScheduler(workerAddr);
            log.info("分配{}给{}", workerAddr, schedulerAddr);
            schedulerDispatchedWorkers.compute(schedulerAddr, (k, v) -> {
                if (v == null) {
                    Set<String> dispatchedWorkers = new ConcurrentHashSet<>();
                    dispatchedWorkers.add(workerAddr);
                    return dispatchedWorkers;
                }
                v.add(workerAddr);
                return v;
            });
        }
        log.info("assignWorkerToScheduler schedulerDispatchedWorkers:{}", schedulerDispatchedWorkers);

    }

    private String getAssignedScheduler(String workerAddr) {
        long hash = Hashing.murmur3_128().hashString(workerAddr, StandardCharsets.UTF_8).asLong();
        return vnConsistentHash.getNode(hash);
    }

    private void removeOnlineWorkerFromScheduler(String workerAddr) {
        // 执行过程中，不能与Scheduler的上下线并发
        synchronized (this) {
            String schedulerAddr = getAssignedScheduler(workerAddr);
            log.info("从{}删除{}", schedulerAddr, workerAddr);
            schedulerDispatchedWorkers.compute(schedulerAddr, (k, v) -> {
                if (v != null) {
                    v.remove(workerAddr);
                    if (v.isEmpty()) {
                        return null;
                    }
                }
                return v;
            });
        }
        log.info("removeOnlineWorkerFromScheduler schedulerDispatchedWorkers:{}", schedulerDispatchedWorkers);
    }

    @EventListener(SchedulerLifecycleEvent.class)
    private void onNewScheduler(SchedulerLifecycleEvent event) throws InterruptedException {
        // 执行过程中，不能与Worker的上下线并发，也不能与scheduler节点的上下线并发，但是zk的监听是串行的，也不用考虑这个
        if (SchedulerLifecycleEvent.Source.ZOOKEEPER == event.getSource()
                && SchedulerLifecycleEvent.SchedulerLifeCycle.ONLINE == event.getLifeCycle()) {
            // 只有完成初始化之后，才能进行后续操作，因为监听zookeeper的事件可能会先于初始化
            log.info("{}节点上线", event.getAddress());
            vnConsistentHashInit.await();
            synchronized (this) {
                this.vnConsistentHash.addNode(event.getAddress());
                this.schedulerDispatchedWorkers.computeIfAbsent(event.getAddress(),(k)->new ConcurrentHashSet<>()); // 分配个hash位置，因为可能新节点没分配到任何节点，导致根本不再这个map里
                // 重新分配节点
                HashMap<String, String> workerOldAssignedScheduler = new HashMap<>();
                this.schedulerDispatchedWorkers.forEach((k, v) -> {
                    for (String workerAddr : v) {
                        String newAssignedScheduler = getAssignedScheduler(workerAddr);
                        if (!StringUtils.equals(k, newAssignedScheduler)) {
                            workerOldAssignedScheduler.put(workerAddr, k);
                        }
                    }
                });

                workerOldAssignedScheduler.forEach((workerAddr, oldSchedulerAddr) -> {
                    log.info("重分配{}", workerAddr);

                    this.schedulerDispatchedWorkers.get(oldSchedulerAddr).remove(workerAddr);
                    this.assignWorkerToScheduler(workerAddr);
                });
            }
            log.info("onNewScheduler schedulerDispatchedWorkers:{}", schedulerDispatchedWorkers);
        }

    }

    @EventListener(SchedulerLifecycleEvent.class)
    private void onRemoveScheduler(SchedulerLifecycleEvent event) throws InterruptedException {

        // 执行过程中，不能与Worker的上下线并发
        if (SchedulerLifecycleEvent.Source.ZOOKEEPER == event.getSource()
                && SchedulerLifecycleEvent.SchedulerLifeCycle.OFFLINE == event.getLifeCycle()) {
            vnConsistentHashInit.await();
            log.info("{}节点下线", event.getAddress());
            synchronized (this) {
                this.vnConsistentHash.deleteNode(event.getAddress());
                for (String workerAddr : this.schedulerDispatchedWorkers.get(event.getAddress())) {
                    log.info("重分配,{}", workerAddr);
                    this.assignWorkerToScheduler(workerAddr);
                }
                this.schedulerDispatchedWorkers.remove(event.getAddress());
            }
            log.info("onRemoveScheduler schedulerDispatchedWorkers:{}", schedulerDispatchedWorkers);
        }
    }
}
