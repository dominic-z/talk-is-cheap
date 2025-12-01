package org.talk.is.cheap.project.free.flow.scheduler.task.service;


import com.google.common.base.VerifyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.enums.StartupSourceType;
import org.talk.is.cheap.project.free.flow.common.enums.TaskStageStatus;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.StartWorkerStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.StartWorkerStageResp;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.task.client.WorkerTaskDriverClient;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageStartupExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskGraphDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskStartupExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageStartupParam;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskGraphDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageExecutionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageStartupService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskExecutionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskGraphDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskStartupService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.StageDefinitionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.StageStartupServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskDefinitionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskExecutionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskStartupServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.StageStartupParamService;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;

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
    private StageDefinitionServiceWrapper stageDefinitionServiceWrapper;
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
    @Autowired
    private StageStartupParamService stageStartupParamService;

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
     * 在启动任务之前校验task，并且在数据库中创建任务数据，包括创建各种startup数据
     *
     * @param taskName
     * @param taskVersion
     */
    public void prepareForTaskStart(String taskName, Integer taskVersion) {
        TaskDefinition taskDefinition = taskDefinitionServiceWrapper.queryByNameVersion(taskName, taskVersion);

        TaskStartup taskStartup = new TaskStartup()
                .withTaskId(taskDefinition.getId())
                .withSourceType(StartupSourceType.EXTERNAL.getValue())
                .withStatus(TaskStageStatus.PENDING.getStatus());
        VerifyUtil.shallBeTrue(taskStartupService.create(taskStartup) > 0, "创建task启动记录失败");


        String workerAddress = taskScheduler.assignTaskToWorkerAddress(taskName, taskDefinition.getVersion(), taskStartup.getId());
        try {
            VerifyUtil.shallNotBeBlank(workerAddress, "No node capable of executing this task can be found.");
        } catch (VerifyException e) {
            TaskStartupExample taskStartupExample = new TaskStartupExample();
            taskStartupExample.createCriteria().andIdEqualTo(taskStartup.getId());
            taskStartupService.updateByExampleSelective(new TaskStartup().withStatus(TaskStageStatus.FAILED.getStatus()),
                    taskStartupExample);
            throw  e;
        }

        TaskExecution taskExecution = new TaskExecution().withStatus(TaskStageStatus.PENDING.getStatus())
                .withAssignedWorkerAddr(workerAddress)
                .withTaskStartupId(taskStartup.getId());
        VerifyUtil.shallBeTrue(taskExecutionService.create(taskExecution) > 0, "创建task执行记录失败");



    }


    /**
     * 尝试驱动fromStageStartupId的下一个stage，返回下一个驱动的stage的startupId
     *
     * @param fromStageStartupId
     * @throws IOException
     */
    public void driveNextStageFrom(long fromStageStartupId) throws IOException {

        StageStartup fromStageStartup = stageStartupServiceWrapper.selectById(fromStageStartupId);
        VerifyUtil.shallBeTrue(fromStageStartup != null,
                "Cannot find a unique stageStartup record with id %d".formatted(fromStageStartupId));

        StageDefinition fromStageDefinition = stageDefinitionServiceWrapper.selectById(fromStageStartup.getStageId());
        VerifyUtil.shallBeTrue(fromStageDefinition != null,
                "Cannot find a unique stageDefinition record with id %d".formatted(fromStageStartup.getStageId()));

        TaskGraphDefinitionExample taskGraphDefinitionExample = new TaskGraphDefinitionExample();
        taskGraphDefinitionExample.createCriteria().andFromStageIdEqualTo(fromStageStartup.getStageId());
        List<TaskGraphDefinition> taskGraphDefinitions = taskGraphDefinitionService.selectByExample(taskGraphDefinitionExample);
        if (taskGraphDefinitions.isEmpty()) {
            StageStartupExample example = new StageStartupExample();
            example.createCriteria().andStageIdIn(taskGraphDefinitions.stream().map(TaskGraphDefinition::getToStageId).toList())
                    .andTaskExecutionIdEqualTo(fromStageStartup.getTaskExecutionId())
                    .andStatusEqualTo(TaskStageStatus.PENDING.getStatus());
            List<StageStartup> nextStageStartups = stageStartupService.selectByExample(example);
            for (StageStartup nextStageStartup : nextStageStartups) {
                driveStage(nextStageStartup.getId());
            }
        }

    }

    //    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    public void driveStage(long stageStartupId) throws IOException {
        StageStartup stageStartup = stageStartupServiceWrapper.selectById(stageStartupId);
        VerifyUtil.shallBeTrue(stageStartup != null,
                "Cannot find a unique stage startup record with id %d".formatted(stageStartupId));

        StageDefinition stageDefinition = stageDefinitionServiceWrapper.selectById(stageStartup.getStageId());

        TaskExecution taskExecution = taskExecutionServiceWrapper.selectById(stageStartup.getTaskExecutionId());
        VerifyUtil.shallBeTrue(taskExecution != null,
                "Cannot find a unique task execution record with id %d".formatted(stageStartup.getTaskExecutionId()));


        TaskStartup taskStartup = taskStartupServiceWrapper.selectById(taskExecution.getTaskStartupId());
        TaskDefinition taskDefinition = taskDefinitionServiceWrapper.selectById(taskStartup.getTaskId());

        StageStartupParam startupParam = stageStartupParamService.getById(stageStartup.getStartupParamEsId());
        // 找到parent 的stage最后一个完成的，取其完成时的共享上下文作为运行时的初始上下文
        TaskGraphDefinitionExample taskGraphDefinitionExample = new TaskGraphDefinitionExample();
        taskGraphDefinitionExample.createCriteria()
                .andToStageIdEqualTo(stageStartup.getStageId());
        List<TaskGraphDefinition> taskGraphDefinitions = taskGraphDefinitionService.selectByExample(taskGraphDefinitionExample);
        List<Long> parentStageIds =
                taskGraphDefinitionService.selectByExample(taskGraphDefinitionExample).stream().map(TaskGraphDefinition::getFromStageId).toList();
        String latestParentStageEncodedSharedContext = null;
        if (!parentStageIds.isEmpty()) {
            StageStartupExample example = new StageStartupExample();
            example.createCriteria()
                    .andTaskExecutionIdEqualTo(stageStartup.getTaskExecutionId())
                    .andStageIdIn(parentStageIds);
            List<StageStartup> parentStageStartups = stageStartupService.selectByExample(example);
            StageStartup latestCompletionStartup = null;
            for (StageStartup parentStageStartup : parentStageStartups) {
                if (parentStageStartup.getCompletionTime() == null) {
                    continue;
                } else if (latestCompletionStartup == null || latestCompletionStartup.getCompletionTime().before(parentStageStartup.getCompletionTime())) {
                    latestCompletionStartup = parentStageStartup;
                }
            }
            if (latestCompletionStartup != null) {
                latestParentStageEncodedSharedContext =
                        stageStartupParamService.getById(latestCompletionStartup.getStartupParamEsId()).getEncodedSharedContextSnapshotAtCompletion();
            }
        }
        startupParam.setUpdateTime(new Date());
        startupParam.setEncodedSharedContextSnapshotAtStartup(latestParentStageEncodedSharedContext);
        stageStartupParamService.update(stageStartup.getStartupParamEsId(), startupParam);


        StartWorkerStageReq startWorkerStageReq = new StartWorkerStageReq();
        StartWorkerStageReq.StageStartupData stageStartupData = new StartWorkerStageReq.StageStartupData();

        stageStartupData.setStageName(stageDefinition.getName());
        stageStartupData.setStageVersion(stageDefinition.getVersion());
        stageStartupData.setStageStartupId(stageStartupId);
        stageStartupData.setTaskStartupId(taskExecution.getTaskStartupId());
        stageStartupData.setTaskName(taskDefinition.getName());
        stageStartupData.setTaskVersion(taskDefinition.getVersion());
        stageStartupData.setEncodedInput(startupParam.getEncodedInput());
        stageStartupData.setEncodedSharedContext(latestParentStageEncodedSharedContext);

        startWorkerStageReq.setData(StartWorkerStageReq.Data.builder().startupDataList(List.of(stageStartupData)).build());

        String workerAddr = taskExecution.getAssignedWorkerAddr();
        URI workerURI = workerClusterManager.getWorkerURI(workerAddr);
        StartWorkerStageResp resp = workerTaskDriverClient.startStage(workerURI, startWorkerStageReq);

        if (!resp.getData().getStageStartResultList().isEmpty() && resp.getData().getStageStartResultList().size() != 1) {
            StartWorkerStageResp.StageStartResult stageStartResult = resp.getData().getStageStartResultList().get(0);
            if (stageStartResult.getResult() == StartWorkerStageResp.StageStartResult.Result.SUCCEEDED) {
                stageStartupServiceWrapper.updateSelectiveById(stageStartupId,
                        new StageStartup().withStatus(TaskStageStatus.RUNNING.getStatus()));
                StageExecution stageExecution = new StageExecution()
                        .withStageStartupId(stageStartupId)
                        .withStatus(TaskStageStatus.RUNNING.getStatus())
                        .withWorkerAddress(workerAddr);
                stageExecutionService.create(stageExecution);
            } else {
                stageStartupServiceWrapper.updateSelectiveById(stageStartupId,
                        new StageStartup().withStatus(TaskStageStatus.FAILED_TO_START.getStatus()));
            }
        }


    }
}
