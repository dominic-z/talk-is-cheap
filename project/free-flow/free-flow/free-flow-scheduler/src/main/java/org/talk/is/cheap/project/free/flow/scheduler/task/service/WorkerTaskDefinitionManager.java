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
import org.talk.is.cheap.project.free.flow.common.message.impl.GetWorkerTaskDefinitionResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.TaskDefinitionDTO;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.event.RunnableWorkerAddEvent;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.task.client.WorkerTaskDefinitionClient;
import org.talk.is.cheap.project.free.flow.starter.repository.config.RepositoryAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNodeLog;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageDefinition;
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
     * 将一些需要事务的数据库操作聚合在这个类里，避免内部调用导致的事务失效问题，还能让代码精简点。内部静态类的好处是，这玩意外面也用不上。。。
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
    private final Map<Tuple2<String, Integer>, Set<String>> taskWorkerMap = new ConcurrentHashMap<>();
    private final Map<Tuple2<String, Integer>, TaskDefinitionDTO> taskDefinitionDTOMap = new ConcurrentHashMap<>();


    // 耗时任务尽可能放在一个独立的线程里，避免影响主线程
    private final ThreadPoolExecutor taskDefinitionThreadPool = new ThreadPoolExecutor(0, 4, 1000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());


    /**
     * 由Scheduler主动询问worker中的任务定义并进行存储，因为目前scheduler就一个，因此由scheduler主动询问可以由scheduler控制访问的并发量。
     *
     * @param event
     */
    @EventListener(RunnableWorkerAddEvent.class)
    public void onRunnableWorkerAddEvent(RunnableWorkerAddEvent event) {
        final String workerAddress = event.getNodeAddress();

        // worker启动时候已经充分校验任务，任务定义本身不需要校验，但是需要考虑并发问题。
        Supplier<Integer> readTaskDefinitionJob = new Supplier<Integer>() {
            @Override
            public Integer get() {
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
                    Tuple2<String, Integer> nameVersion = new Tuple2<>(taskDefinitionDTO.getName(), taskDefinitionDTO.getVersion());
                    taskDefinitionDTOMap.put(nameVersion, taskDefinitionDTO);
                    workerTaskMap.get(workerAddress).add(nameVersion);
                    taskWorkerMap.putIfAbsent(nameVersion, new HashSet<>());
                    taskWorkerMap.get(nameVersion).add(workerAddress);

                    example.clear();
                    example.createCriteria().andNameEqualTo(taskDefinitionDTO.getName())
                            .andVersionEqualTo(taskDefinitionDTO.getVersion());
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
                return taskDefinitionDTOList.size();
            }
        };

        CompletableFuture.supplyAsync(readTaskDefinitionJob, taskDefinitionThreadPool)
                .thenAccept(n -> log.info("Successfully read the {} task definition from the worker node {}.", n, workerAddress))
                .exceptionally(e -> {
                    log.error("Fail to read the task definition from the worker node {}.", workerAddress, e);
                    return null;
                });
    }
}
