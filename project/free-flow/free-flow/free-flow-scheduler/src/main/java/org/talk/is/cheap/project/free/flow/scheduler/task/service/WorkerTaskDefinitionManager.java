package org.talk.is.cheap.project.free.flow.scheduler.task.service;


import io.vavr.Tuple2;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talk.is.cheap.project.free.flow.common.enums.TaskStageStatus;
import org.talk.is.cheap.project.free.flow.common.message.impl.GetWorkerTaskDefinitionResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.TaskDefinitionDTO;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.event.RunnableWorkerAddEvent;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.event.WorkerTerminatedEvent;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.task.client.WorkerTaskDefinitionClient;
import org.talk.is.cheap.project.free.flow.scheduler.util.FieldAwareLockManager;
import org.talk.is.cheap.project.free.flow.starter.repository.config.RepositoryAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskGraphDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNodeLog;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskGraphDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.service.ClusterNodeLogService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskGraphDefinitionService;

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
            taskDefinitionService.create(taskDefinition);
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
    private StageDefinitionService stageDefinitionService;
    @Autowired
    private TaskGraphDefinitionService taskGraphDefinitionService;
    @Autowired
    private WorkerTaskDefinitionManagerTxnHelper workerTaskDefinitionManagerTxnHelper;

    @Autowired
    private WorkerClusterManager workerClusterManager;

    @Getter
    private static final ModelMapper MODEL_MAPPER = new ModelMapper();

    // 存储每个workerAddress中包含的task定义情况。两个map互为倒排
    private final Map<String, Set<Tuple2<String, Integer>>> workerTaskMap = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, Set<String>>> taskWorkerMap = new ConcurrentHashMap<>(); // taskName->taskVerison->workerAddress
    private final Map<String, Map<Integer, TaskDefinitionDTO>> taskDefinitionDTOMap = new ConcurrentHashMap<>();

    private final FieldAwareLockManager<String> lockManagerByWorkerAddress = new FieldAwareLockManager<>();

    // 耗时任务尽可能放在一个独立的线程里，避免影响主线程
    private final ThreadPoolExecutor taskDefinitionThreadPool = new ThreadPoolExecutor(0, 4, 1000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());


    /**
     * 由Scheduler主动询问worker中的任务定义并进行存储，因为目前scheduler就一个，因此由scheduler主动询问可以由scheduler控制访问的并发量。
     *
     *
     * 因为处理addEvent和removeEvent的先后顺序无法确保，例如同一个NodeAddress，可能上线后立刻断开链接，但是terminated事件先于addEvent处理完成
     * 所以本类中存在的worker并不一定是真实存活的worker，真实存活的worker由workerClusterManager确保
     *
     * 并发处理，可能存在一个worker刚触发了addevent正在读取信息的过程中，又触发了workerTerminiated事件，导致这个对象里多个map的数据不一致
     * 通过加锁解决，加了锁之后，上面的问题也一并解决了，即使terminated事件先处理了，addEvent后处理的时候也会抛出异常，而不会成功读取worker的任务定义（因为已经断开链接了）
     *
     * @param event
     */
    @EventListener(RunnableWorkerAddEvent.class)
    public void onRunnableWorkerAddEvent(RunnableWorkerAddEvent event) {
        final String workerAddress = event.getNodeAddress();

        // worker启动时候已经充分校验任务，任务定义本身不需要校验，但是需要考虑并发问题。
        Supplier<Integer> readTaskDefinitionJob = () -> {
            lockManagerByWorkerAddress.lock(workerAddress);

            GetWorkerTaskDefinitionResp getWorkerTaskDefinitionResp =
                    workerTaskDefinitionClient.getTaskDefinition(workerClusterManager.getWorkerURI(workerAddress));
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
                taskDefinitionDTOMap.computeIfAbsent(taskName, (key) -> new ConcurrentHashMap<>()).put(taskVersion, taskDefinitionDTO);
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
                }

            }

            lockManagerByWorkerAddress.unlockAndRemove(workerAddress);
            return taskDefinitionDTOList.size();
        };

        CompletableFuture.supplyAsync(readTaskDefinitionJob, taskDefinitionThreadPool)
                .thenAccept(n -> log.info("Successfully read the {} task definition from the worker node {}.", n, workerAddress))
                .exceptionally(e -> {
                    log.error("Fail to read the task definition from the worker node {}.", workerAddress, e);
                    return null;
                });
    }

    @EventListener(WorkerTerminatedEvent.class)
    public void onWorkerTerminated(WorkerTerminatedEvent event){
        String nodeAddress = event.getNodeAddress();

        Supplier<Boolean> workerRemovedJob = ()->{
            lockManagerByWorkerAddress.lock(nodeAddress);
            Set<Tuple2<String, Integer>> removedTaskVersion = this.workerTaskMap.remove(nodeAddress);
            for (Tuple2<String, Integer> nameVersion : removedTaskVersion) {
                String taskName = nameVersion._1();
                Integer taskVersion = nameVersion._2();

                if(this.taskWorkerMap.containsKey(taskName) && this.taskWorkerMap.get(taskName).containsKey(taskVersion)){
                    this.taskWorkerMap.get(taskName).get(taskVersion).remove(nodeAddress);
                }
            }
            lockManagerByWorkerAddress.unlockAndRemove(nodeAddress);
            return true;
        };

        CompletableFuture.supplyAsync(workerRemovedJob, taskDefinitionThreadPool)
                .thenAccept(n -> log.info("The {} node is offline, and the task definition data it held has been successfully cleared.", nodeAddress))
                .exceptionally(e -> {
                    log.error("The {} node is offline, and an exception occurred while clearing the task definition data it held.", nodeAddress);
                    return null;
                });
    }


    /**
     * 获取具备某个任务的worker列表
     * @param taskName
     * @param taskVersion
     * @return
     */
    public Set<String> getWorkerAddressesWithTask(String taskName, Integer taskVersion) {
        Map<Integer, Set<String>> versionAddresses = this.taskWorkerMap.get(taskName);
        if (taskVersion != null) {
            if (versionAddresses.containsKey(taskVersion)) {
                return new HashSet<>();
            }
            return new HashSet<>(versionAddresses.get(taskVersion));
        }

        return versionAddresses.values().stream().reduce(new HashSet<>(), (s1, s2) -> {
            s1.addAll(s2);
            return s1;
        });
    }
}
