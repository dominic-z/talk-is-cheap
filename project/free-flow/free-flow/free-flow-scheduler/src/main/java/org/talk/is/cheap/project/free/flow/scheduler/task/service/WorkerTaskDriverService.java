package org.talk.is.cheap.project.free.flow.scheduler.task.service;


import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talk.is.cheap.project.free.flow.common.enums.StartupSourceType;
import org.talk.is.cheap.project.free.flow.common.enums.TaskStageStatus;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.task.client.WorkerTaskDriverClient;
import org.talk.is.cheap.project.free.flow.starter.repository.config.RepositoryAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageStartupExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.ESPojoDTO;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageStartupParam;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.TaskSharedContext;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageExecutionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageStartupService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskExecutionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskGraphDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskStartupService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.StageDefinitionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.StageExecutionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.StageStartupServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskDefinitionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskExecutionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskStartupServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.StageStartupParamService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.TaskSharedContextService;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WorkerTaskDriverService {


    /**
     * 数据库操作service
     */
    @Autowired
    private StageStartupServiceWrapper stageStartupServiceWrapper;
    @Autowired
    private StageStartupService stageStartupService;
    private StageExecutionService stageExecutionService;
    @Autowired
    private StageExecutionServiceWrapper stageExecutionServiceWrapper;
    @Autowired
    private StageDefinitionServiceWrapper stageDefinitionServiceWrapper;
    @Autowired
    private StageDefinitionService stageDefinitionService;
    @Autowired
    private TaskGraphDefinitionService taskGraphDefinitionService;
    @Autowired
    private TaskDefinitionServiceWrapper taskDefinitionServiceWrapper;
    @Autowired
    private TaskStartupService taskStartupService;
    @Autowired
    private TaskStartupServiceWrapper taskStartupServiceWrapper;
    @Autowired
    private TaskExecutionServiceWrapper taskExecutionServiceWrapper;
    @Autowired
    private TaskExecutionService taskExecutionService;

    // es
    @Autowired
    private StageStartupParamService stageStartupParamService;

    @Autowired
    private TaskSharedContextService taskSharedContextService;

    /**
     * worker客户端
     */
    @Autowired
    private WorkerTaskDriverClient workerTaskDriverClient;

    /**
     * 任务与集群管理service
     */
    @Autowired
    private WorkerClusterManager workerClusterManager;
    @Autowired
    private TaskScheduler taskScheduler;

    /**
     * 在启动任务之前校验task，并且在数据库中创建任务数据，包括创建各种startup+execution数据，以便可以直接执行任务
     * 不包括retry的情况，retry的执行不需要创建startup
     * 引导worker设置初始化的共享上下文
     *
     * @param taskName
     * @param taskVersion 返回的是创建的执行任务的地址，taskExecutionId和stageExecutionIds
     */
//    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    public Tuple3<String, Long, Map<String, Long>> prepareForTaskStart(String taskName, Integer taskVersion,
                                                                       String initialEncodedSharedContext,
                                                                       Map<String, String> stageEncodedInputs) {
        TaskDefinition taskDefinition = taskDefinitionServiceWrapper.queryByNameVersion(taskName, taskVersion);

        // 创建task的startup和execution
        TaskStartup taskStartup = new TaskStartup()
                .withTaskId(taskDefinition.getId())
                .withSourceType(StartupSourceType.EXTERNAL.getValue())
                .withStatus(TaskStageStatus.PENDING.getStatus());
        VerifyUtil.shallBeTrue(taskStartupService.create(taskStartup) > 0, "创建task启动记录失败");

        try {
            taskSharedContextService.create(TaskSharedContext.builder()
                    .taskStartupId(taskStartup.getId())
                    .encodedTaskSharedContext(initialEncodedSharedContext)
                    .updateTime(new Date()).build()
            );

            String workerAddress = taskScheduler.assignTaskToWorkerAddress(taskName, taskDefinition.getVersion());
            VerifyUtil.shallNotBeBlank(workerAddress, "No node capable of executing this task can be found.");

            TaskExecution taskExecution = new TaskExecution()
                    .withStatus(TaskStageStatus.PENDING.getStatus())
                    .withAssignedWorkerAddr(workerAddress)
                    .withTaskStartupId(taskStartup.getId());
            VerifyUtil.shallBeTrue(taskExecutionService.create(taskExecution) > 0, "创建task执行记录失败");

            // 创建stage的startup和execution
            List<StageDefinition> stageDefinitions = stageDefinitionServiceWrapper.selectByTaskId(taskDefinition.getId());
            Map<String, Long> rootStageName2ExecutionId = new HashMap<>();
            for (StageDefinition stageDefinition : stageDefinitions) {
                String stageName = stageDefinition.getName();


                StageStartup stageStartup = new StageStartup()
                        .withTaskExecutionId(taskExecution.getId())
                        .withStatus(TaskStageStatus.PENDING.getStatus())
                        .withStageId(stageDefinition.getId());

                VerifyUtil.shallBeTrue(stageStartupService.create(stageStartup) > 0, String.format("Failed to create stage startup: %s",
                        stageName));
                StageExecution stageExecution = new StageExecution()
                        .withStageStartupId(stageStartup.getId())
                        .withStatus(TaskStageStatus.PENDING.getStatus())
                        .withWorkerAddress(workerAddress);
                VerifyUtil.shallBeTrue(stageExecutionService.create(stageExecution) > 0, String.format("Failed to create stage execution:" +
                        " %s", stageName));

                if (stageEncodedInputs.containsKey(stageName)) {
                    String encodedInput = stageEncodedInputs.get(stageName);
                    stageStartupParamService.create(
                            StageStartupParam.builder().stageStartupId(stageStartup.getId())
                                    .encodedInput(encodedInput)
                                    .encodedSharedContextSnapshotAtStartup(initialEncodedSharedContext)
                                    .updateTime(new Date())
                                    .build()
                    );
                }

                if (stageDefinition.getIsStartingStage()) {
                    rootStageName2ExecutionId.put(stageName, stageExecution.getId());
                }
            }


            return new Tuple3<>(workerAddress, taskExecution.getId(), rootStageName2ExecutionId);
        } catch (IOException e) {
            log.error("error when prepare task", e);
            throw new RuntimeException(e);
        } finally {
            taskStartup.setStatus(TaskStageStatus.FAILED.getStatus());
            taskStartupServiceWrapper.updateByIdSelective(taskStartup.getId(), taskStartup);
        }


    }


    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    public Tuple2<Long, String> prepareStage(long taskExecutionId, String stageName,
                                             String encodedSharedContextSnapshotAtStartup) throws IOException {
        TaskExecution taskExecution = taskExecutionServiceWrapper.selectById(taskExecutionId);
        VerifyUtil.shallNotBeNull(taskExecution, String.format("无法找到id为%d的taskExecution对象", taskExecutionId));

        TaskStartup taskStartup = taskStartupServiceWrapper.selectById(taskExecution.getTaskStartupId());

        StageDefinition stageDefinition = stageDefinitionServiceWrapper.selectByTaskIdStageName(taskStartup.getTaskId(), stageName);
        VerifyUtil.shallNotBeNull(stageDefinition,
                String.format("taskId为%d的任务定义中无法找到唯一的一个name为%s的stage", taskStartup.getTaskId(), stageName));


        StageStartupExample stageStartupExample = new StageStartupExample();
        stageStartupExample.createCriteria()
                .andTaskExecutionIdEqualTo(taskExecutionId)
                .andStageIdEqualTo(stageDefinition.getId())
                .andStatusEqualTo(TaskStageStatus.PENDING.getStatus());
        List<StageStartup> stageStartups = stageStartupService.selectByExample(stageStartupExample);
        VerifyUtil.shallBeTrue(stageStartups.size() == 1,
                String.format("无法找到stage startup对象（taskExecutionId: %d, stageId: %d, status: %d）", taskExecutionId,
                        stageDefinition.getId(), TaskStageStatus.PENDING.getStatus())
        );
        StageStartup stageStartup = stageStartups.get(0);
        stageStartupServiceWrapper.updateSelectiveById(stageStartup.getId(),new StageStartup().withStatus(TaskStageStatus.RUNNING.getStatus()));

        ESPojoDTO<StageStartupParam> stageStartupParamESPojoDTO = stageStartupParamService.getByStageStartupId(stageStartup.getId());
        stageStartupParamESPojoDTO.getData().setEncodedSharedContextSnapshotAtStartup(encodedSharedContextSnapshotAtStartup);
        stageStartupParamService.update(stageStartupParamESPojoDTO.getId(),stageStartupParamESPojoDTO.getData());

        List<StageExecution> stageExecutions = stageExecutionServiceWrapper.selectByStartupId(stageStartup.getId(),
                TaskStageStatus.PENDING.getStatus());
        VerifyUtil.shallBeTrue(stageExecutions.size() == 1,
                String.format("无法找到StageExecution对象（startUpId: %d, status: %d）", stageStartup.getId(),
                        TaskStageStatus.PENDING.getStatus()));
        StageExecution stageExecution = stageExecutions.get(0);
        stageExecutionServiceWrapper.updateSelectiveById(stageExecution.getId(),new StageExecution().withStatus(TaskStageStatus.RUNNING.getStatus()));

        return new Tuple2<>(stageStartup.getId(),stageStartupParamESPojoDTO.getData().getEncodedInput());

    }
}
