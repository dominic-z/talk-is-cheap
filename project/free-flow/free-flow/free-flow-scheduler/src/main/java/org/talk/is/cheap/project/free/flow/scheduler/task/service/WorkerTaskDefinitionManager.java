package org.talk.is.cheap.project.free.flow.scheduler.task.service;


import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.message.impl.GetWorkerTaskDefinitionResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.TaskDefinitionDTO;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.event.RunnableWorkerAddEvent;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.task.client.WorkerTaskDefinitionClient;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskDefinitionService;

import java.util.ArrayList;
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

    @Autowired
    private WorkerTaskDefinitionClient workerTaskDefinitionClient;

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private WorkerClusterManager workerClusterManager;

    private final ModelMapper modelMapper = new ModelMapper();

    // 存储每个workerAddress中包含的task定义情况。两个map
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

        // worker启动时候已经充分校验任务，直接存就好了。
        Supplier<Integer> readTaskDefinitionJob = new Supplier<Integer>() {
            @Override
            public Integer get() {
                GetWorkerTaskDefinitionResp getWorkerTaskDefinitionResp = workerTaskDefinitionClient.getTaskDefinition(workerClusterManager.getWorkerURI(workerAddress));
                List<TaskDefinitionDTO> taskDefinitionDTOList = getWorkerTaskDefinitionResp.getData();

                if (taskDefinitionDTOList == null) {
                    log.warn("worker: {} has no task definition", workerAddress);
                    return 0;
                }
                final int batchSize = 10;
                final List<TaskDefinitionDTO> buffer = new ArrayList<>();
                TaskDefinitionExample example = new TaskDefinitionExample();
                workerTaskMap.put(workerAddress, new HashSet<>());

                for (int i = 0; i < taskDefinitionDTOList.size(); i++) {
                    TaskDefinitionDTO taskDefinitionDTO = taskDefinitionDTOList.get(i);
                    Tuple2<String, Integer> nameVersion = new Tuple2<>(taskDefinitionDTO.getName(), taskDefinitionDTO.getVersion());
                    taskDefinitionDTOMap.put(nameVersion,taskDefinitionDTO);
                    workerTaskMap.get(workerAddress).add(nameVersion);
                    taskWorkerMap.putIfAbsent(nameVersion,new HashSet<>());
                    taskWorkerMap.get(nameVersion).add(workerAddress);

                    buffer.add(taskDefinitionDTO);
                    example.or().andNameEqualTo(taskDefinitionDTO.getName())
                            .andVersionEqualTo(taskDefinitionDTO.getVersion());
                    if (taskDefinitionDTOList.size() % batchSize == 0 || i == taskDefinitionDTOList.size() - 1) {
                        Map<Tuple2<String, Integer>, TaskDefinition> taskDefinitionMap =
                                taskDefinitionService.selectByExample(example).stream()
                                        .collect(Collectors.toMap(td -> new Tuple2<String, Integer>(td.getName(), td.getVersion()), td -> td));

                        List<TaskDefinition> newTaskDefinitions = new ArrayList<>();
                        for (TaskDefinitionDTO definitionDTO : buffer) {
                            if (!taskDefinitionMap.containsKey(
                                    new Tuple2<String, Integer>(definitionDTO.getName(), definitionDTO.getVersion()))) {

                                TaskDefinition taskDefinition = modelMapper.map(taskDefinitionDTO, TaskDefinition.class);
                                newTaskDefinitions.add(taskDefinition);
                            }
                        }
                        taskDefinitionService.createBatch(newTaskDefinitions);
                        buffer.clear();
                    }
                }
                return taskDefinitionDTOList.size();
            }
        };

        CompletableFuture.supplyAsync(readTaskDefinitionJob,taskDefinitionThreadPool)
                .thenAccept(n->log.info("Successfully read the {} task definition from the worker node {}.",n,workerAddress))
                .exceptionally(e->{
                    log.error("Fail to read the task definition from the worker node {}.",workerAddress,e);
                    return null;
                });
    }
}
