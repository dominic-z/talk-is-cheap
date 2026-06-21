package org.talk.is.cheap.project.free.flow.scheduler.task.service;


import io.vavr.Tuple2;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talk.is.cheap.project.free.flow.common.enums.TaskDefinitionStatus;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.GetWorkerTaskDefinitionResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.TaskDefinitionDTO;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.event.WatchWorkerTaskDefinitionEvent;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.config.property.ZKPathProperty;
import org.talk.is.cheap.project.free.flow.scheduler.task.client.WorkerTaskDefinitionClient;
import org.talk.is.cheap.project.free.flow.common.utils.FieldAwareLockManager;
import org.talk.is.cheap.project.free.flow.starter.repository.config.RepositoryAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskGraphDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskGraphDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskDefinitionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.redis.RedisService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.talk.is.cheap.project.free.flow.starter.repository.config.RedisAutoConfig.STRING_REDIS_TEMPLATE;

/**
 * 通过zk监听已经建立链接的worker，读取其中的任务定义
 * 目前只有leader才能有完整的WorkerTaskDefinitionManager，有点问题。
 */
@Service
@Slf4j
public class WorkerTaskDefinitionManager {


    /**
     * 将一些需要事务的数据库操作聚合在这个类里，而不是直接将方法写在WorkerTaskDefinitionManager，否则会内部调用导致的事务失效问题，还能让代码精简点。内部静态类的好处是，这玩意外面也用不上。。。
     */
    @Service
    @Slf4j
    public static class WorkerTaskDefinitionManagerTxnHelper {
        @Autowired
        private TaskDefinitionService taskDefinitionService;
        @Autowired
        private StageDefinitionService stageDefinitionService;
        @Autowired
        private TaskGraphDefinitionService taskGraphDefinitionService;

        @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
        public void createTask(TaskDefinitionDTO taskDefinitionDTO) throws Exception {
            TaskDefinition taskDefinition = MODEL_MAPPER.map(taskDefinitionDTO, TaskDefinition.class);
            taskDefinitionService.create(taskDefinition); // 不必担心重复创建，数据表里有唯一索引控制
            // 创建stageDefinition
            List<StageDefinition> stageDefinitions = taskDefinitionDTO.getStageDefinitionMap().values().stream().
                    map(dto -> {
                        StageDefinition stageDefinition = MODEL_MAPPER.map(dto, StageDefinition.class);
                        stageDefinition.setTaskId(taskDefinition.getId());
                        return stageDefinition;
                    })
                    .toList();

            stageDefinitionService.createBatchSelective(stageDefinitions,
                    Set.of(StageDefinition.CREATE_TIME, StageDefinition.UPDATE_TIME, StageDefinition.REVISION));


            // 创建taskGraphDefinition
            StageDefinitionExample example = new StageDefinitionExample();
            example.createCriteria().andTaskIdEqualTo(taskDefinition.getId());
            Map<String, Long> stageNameIdMap = stageDefinitionService.selectByExample(example).stream()
                    .collect(Collectors.toMap(StageDefinition::getName, StageDefinition::getId));

            List<TaskGraphDefinition> taskGraphDefinitions = taskDefinitionDTO.getPointOutGraph().entrySet().stream()
                    .flatMap(kv -> {
                        Long fromStageId = stageNameIdMap.get(kv.getKey());
                        return kv.getValue().stream().map(toStageName -> {
                            TaskGraphDefinition taskGraphDefinition = new TaskGraphDefinition();
                            taskGraphDefinition.setTaskId(taskDefinition.getId());
                            taskGraphDefinition.setFromStageId(fromStageId);
                            taskGraphDefinition.setToStageId(stageNameIdMap.get(toStageName));
                            return taskGraphDefinition;
                        });
                    }).toList();
            taskGraphDefinitionService.createBatchSelective(taskGraphDefinitions,
                    Set.of(TaskGraphDefinition.UPDATE_TIME, TaskGraphDefinition.CREATE_TIME, TaskGraphDefinition.REVISION));
        }
    }

    @Autowired
    private WorkerTaskDefinitionClient workerTaskDefinitionClient;

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private TaskDefinitionServiceWrapper taskDefinitionServiceWrapper;
    @Autowired
    private StageDefinitionService stageDefinitionService;
    @Autowired
    private TaskGraphDefinitionService taskGraphDefinitionService;
    @Autowired
    private WorkerTaskDefinitionManagerTxnHelper workerTaskDefinitionManagerTxnHelper;

    @Autowired
    private WorkerClusterManager workerClusterManager;

    @Autowired
    private CuratorFramework curatorZKClient;

    @Autowired
    private ZKPathProperty zkPathProperty;


    @Autowired
    @Qualifier(STRING_REDIS_TEMPLATE)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisService redisService;

    @Getter
    private static final ModelMapper MODEL_MAPPER = new ModelMapper();

    // 存储自己管理的workerAddress中包含的task定义情况。两个map互为倒排
    private final Map<String, Set<Tuple2<String, Integer>>> workerTaskMap = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, Set<String>>> taskWorkerMap = new ConcurrentHashMap<>(); // taskName->taskVerison->workerAddress

    private final FieldAwareLockManager<String> lockManagerByWorkerAddress = new FieldAwareLockManager<>();

    // 耗时任务尽可能放在一个独立的线程里，避免影响主线程
    private final ThreadPoolExecutor taskDefinitionThreadPool = new ThreadPoolExecutor(4, 4, 1000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    private CuratorCache curatorCache;

    @PostConstruct
    public void init() {
        MODEL_MAPPER
                .typeMap(TaskDefinitionDTO.class, TaskDefinition.class)
                .addMappings(mapper -> {
                    mapper.skip((d, v) -> {
                        d.setStatus(TaskDefinitionStatus.HAS_AVAILABLE_WORKER.getType());
                    });
                });
    }


    @EventListener(WatchWorkerTaskDefinitionEvent.class)
    public void watchRunnableWorkerForDefinition(WatchWorkerTaskDefinitionEvent e) {
        if (e.isNeedInit()) {
            watchRunnableWorker();
        } else {
            for (String workerAddr : e.getWorkerAddrs()) {
                onAddRunnableWorkerEvent(workerAddr);
            }
        }
    }

    private void watchRunnableWorker() {
        curatorCache = CuratorCache.builder(curatorZKClient, zkPathProperty.getWorker().getRunnable()).build();
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forPathChildrenCache(zkPathProperty.getWorker().getRunnable(), curatorZKClient, new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {

                        try {
                            if (event.getData() == null) {
                                log.info("on runnable worker event: {}", event.getType());
                            } else {
                                String workerAddress = new String(event.getData().getData());
                                log.info("on runnable worker event: {}, zkPath: {}, address: {}", event.getType(),
                                        event.getData().getPath(),
                                        workerAddress);
                                if (workerClusterManager.isManagedWorkerAddrs(workerAddress)) {
                                    switch (event.getType()) {
                                        case CHILD_ADDED:
                                            onAddRunnableWorkerEvent(workerAddress);
                                            break;
                                        case CHILD_REMOVED:
                                            onRemoveRunnableWorker(workerAddress);
                                            break;
                                    }
                                } else {
                                    log.info("不是自己管理的节点{}，跳过", workerAddress);
                                }
                            }
                        } catch (Exception e) {
                            log.error("监听{}的事件处理失败", zkPathProperty.getWorker().getRunnable(), e);
                        }
                    }
                }).build();

        curatorCache.listenable().addListener(listener);
        curatorCache.start();
    }


    @PreDestroy
    public void stop() {
        curatorCache.close();
    }

    /**
     * 并发处理，可能存在一个worker刚触发了addevent正在读取信息的过程中，又触发了workerTerminiated事件，导致这个对象里多个map的数据不一致
     * 通过加锁解决，加了锁之后，上面的问题也一并解决了，即使terminated事件先处理了，addEvent后处理的时候也会抛出异常，而不会成功读取worker的任务定义（因为已经断开链接了）
     */
    private void onAddRunnableWorkerEvent(String workerAddress) {

        // worker启动时候已经充分校验任务，任务定义本身不需要校验，但是需要考虑并发问题。
        Supplier<Integer> readTaskDefinitionJob = () -> {
            try {
                // 加锁，避免节点反复上下导致并发问题
                lockManagerByWorkerAddress.lock(workerAddress);

                if (workerTaskMap.containsKey(workerAddress)) {
                    // 如果一个节点已经读取过，就不必重新读取了，直接返回；按理来说不会出现这个情况；
                    log.info("The reading phase has been skipped for {}.", workerAddress);
                    return -1;
                }

                GetWorkerTaskDefinitionResp getWorkerTaskDefinitionResp =
                        workerTaskDefinitionClient.getTaskDefinition(WorkerClusterManager.getWorkerURI(workerAddress));
                VerifyUtil.requireTrue(getWorkerTaskDefinitionResp.isSuccess(), String.format("向worker请求%s获取任务定义失败，失败原因:%s",
                        workerAddress, getWorkerTaskDefinitionResp.getMsg()));
                List<TaskDefinitionDTO> taskDefinitionDTOList = getWorkerTaskDefinitionResp.getData();

                if (taskDefinitionDTOList == null) {
                    log.warn("worker: {} has no task definition", workerAddress);
                    return 0;
                }
                final int batchSize = 10;
                TaskDefinitionExample example = new TaskDefinitionExample();
                workerTaskMap.put(workerAddress, new HashSet<>());

                for (TaskDefinitionDTO taskDefinitionDTO : taskDefinitionDTOList) {
                    String taskName = taskDefinitionDTO.getName();
                    Integer taskVersion = taskDefinitionDTO.getVersion();
                    Tuple2<String, Integer> nameVersion = new Tuple2<>(taskName, taskVersion);
                    workerTaskMap.get(workerAddress).add(nameVersion);
                    taskWorkerMap.computeIfAbsent(taskName, (n) -> new ConcurrentHashMap<>())
                            .computeIfAbsent(taskVersion, (v) -> new HashSet<>())
                            .add(workerAddress);

                    example.clear();
                    example.createCriteria().andNameEqualTo(taskName)
                            .andVersionEqualTo(taskVersion);
                    if (taskDefinitionService.selectByExample(example).isEmpty()) {
                        try {
                            workerTaskDefinitionManagerTxnHelper.createTask(taskDefinitionDTO);
                        } catch (Exception e) {
                            log.error("Writing task definition failed. This may be caused by concurrent writes. " +
                                    "It is recommended to check whether the task definition is created normally, but this will not affect" +
                                    " the worker's execution of this task.", e);
                        }
                    } else {
                        taskDefinitionService.updateByExampleSelective(new TaskDefinition()
                                .withStatus(TaskDefinitionStatus.HAS_AVAILABLE_WORKER.getType()), example);
                    }
                    stringRedisTemplate.opsForSet().add(RedisService.getTaskWorkerAddrMapKey(taskName, taskVersion), workerAddress);
                    log.info("finish parsing task: {}, version: {} from {}", taskName, taskVersion, workerAddress);
                }
                return taskDefinitionDTOList.size();
            } catch (InterruptedException e) {
                log.error("加锁失败", e);
                throw new RuntimeException(e);
            } finally {
                lockManagerByWorkerAddress.tryUnlock(workerAddress);
            }


        };

        CompletableFuture.supplyAsync(readTaskDefinitionJob, taskDefinitionThreadPool)
                .thenAccept(n -> log.info("Successfully read  {} task definition from the worker node {}.", n, workerAddress))
                .exceptionally(e -> {
                    log.error("Fail to read the task definition from the worker node {}.", workerAddress, e);
                    return null;
                });
    }

    public void onRemoveRunnableWorker(String nodeAddress) {

        Supplier<Boolean> workerRemovedJob = () -> {
            try {
                lockManagerByWorkerAddress.lock(nodeAddress);
                Set<Tuple2<String, Integer>> removedTaskVersion = this.workerTaskMap.remove(nodeAddress);
                for (Tuple2<String, Integer> nameVersion : removedTaskVersion) {
                    String taskName = nameVersion._1();
                    Integer taskVersion = nameVersion._2();

                    if (this.taskWorkerMap.containsKey(taskName) && this.taskWorkerMap.get(taskName).containsKey(taskVersion)) {
                        this.taskWorkerMap.get(taskName).get(taskVersion).remove(nodeAddress);
                    }
                    String taskWorkerAddrMapKey = RedisService.getTaskWorkerAddrMapKey(taskName, taskVersion);
                    stringRedisTemplate.opsForSet().remove(taskWorkerAddrMapKey, nodeAddress);
                    if (redisService.deleteEmptySet(taskWorkerAddrMapKey)) {
                        taskDefinitionServiceWrapper.updateSelectiveByNameVersion(taskName, taskVersion,
                                new TaskDefinition().withStatus(TaskDefinitionStatus.HAS_NO_AVAILABLE_WORKER.getType()), null);
                    }
                }
            } catch (InterruptedException e) {
                log.error("加锁失败", e);
                throw new RuntimeException(e);
            } finally {
                lockManagerByWorkerAddress.tryUnlock(nodeAddress);
            }
            return true;
        };

        CompletableFuture.supplyAsync(workerRemovedJob, taskDefinitionThreadPool)
                .thenAccept(n -> log.info("The {} node is offline, and the task definition data it held has been successfully cleared.",
                        nodeAddress))
                .exceptionally(e -> {
                    log.error("The {} node is offline, and an exception occurred while clearing the task definition data it held.",
                            nodeAddress);
                    return null;
                });
    }


    // 通过redis，所有的scheduler都有除了启动任务之外的能力了。leader只需要管理redis里的数据就好。
    public Set<String> getWorkerAddressesWithTaskRedis(String taskName, Integer taskVersion) {

        String taskWorkerAddrMapKey = RedisService.getTaskWorkerAddrMapKey(taskName, taskVersion);
        Set<String> addrs = stringRedisTemplate.opsForSet().members(taskWorkerAddrMapKey);
        if (addrs == null) {
            return new HashSet<>();
        }

        // 加一个判断，如果scheduler挂了的同时worker也挂了，那么redis里会存在脏数据，即挂掉的worker还在
        Set<String> refreshedAddrs = addrs.stream().filter(addr -> {
            if (workerClusterManager.isValidRunnableWorker(addr)) {
                return true;
            }
            stringRedisTemplate.opsForSet().remove(taskWorkerAddrMapKey, addr);
            redisService.deleteEmptySet(taskWorkerAddrMapKey);
            return false;
        }).collect(Collectors.toSet());
        return refreshedAddrs;
    }


}
