package org.talk.is.cheap.project.free.flow.scheduler.task.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talk.is.cheap.project.free.flow.common.enums.TaskStageStatus;
import org.talk.is.cheap.project.free.flow.common.message.impl.WorkerSubmitStageResultReq;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.repository.config.RepositoryAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageExecutionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageStartupExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskExecutionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskGraphDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageExecutionResultMsg;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageStartupParam;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.TaskSharedContext;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskGraphDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageExecutionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageStartupService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskExecutionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskGraphDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.StageStartupServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskExecutionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.StageExecutionResultMsgService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.StageStartupParamService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.TaskSharedContextService;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class WorkerTaskResultService {

    @Autowired
    private StageStartupServiceWrapper stageStartupServiceWrapper;
    @Autowired
    private StageExecutionService stageExecutionService;

    @Autowired
    private StageExecutionResultMsgService stageExecutionResultMsgService;
    @Autowired
    private TaskExecutionServiceWrapper taskExecutionServiceWrapper;
    @Autowired
    private TaskSharedContextService taskSharedContextService;
    @Autowired
    private StageStartupParamService stageStartupParamService;

    /**
     * 某个stage已经完成，记录shareContext快照，并且尝试驱动下一个stage
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    public void saveStageResult(WorkerSubmitStageResultReq.StageResult stageResult) throws IOException {

        Long stageStartupId = stageResult.getStageStartupId();
        // 更新stage startup
        StageStartup stageStartup = stageStartupServiceWrapper.selectById(stageStartupId);
        VerifyUtil.shallBeTrue(stageStartup != null,
                "The startup record for the stage with ID %d does not exist.".formatted(stageStartupId));
        Integer taskStageStatus = stageResult.getSucceeded() ? TaskStageStatus.SUCCEEDED.getStatus() :
                TaskStageStatus.FAILED.getStatus();
        stageStartupServiceWrapper.updateSelectiveById(stageStartupId,
                new StageStartup().withStatus(taskStageStatus).withCompletionTime(stageResult.getCompletionTime()));


        // 更新stage execution
        StageExecutionExample stageExecutionExample = new StageExecutionExample();
        stageExecutionExample.createCriteria().andStageStartupIdEqualTo(stageStartupId).andStatusEqualTo(TaskStageStatus.RUNNING.getStatus());

        List<StageExecution> stageExecutions = stageExecutionService.selectByExample(stageExecutionExample);
        VerifyUtil.shallBeTrue(stageExecutions.size() == 1,
                "The running execution record for the stage with startup ID %d does not exist.".formatted(stageStartupId));
        StageExecution stageExecution = stageExecutions.get(0);
        stageExecutionService.updateByExampleSelective(new StageExecution().withStatus(taskStageStatus), stageExecutionExample);

        // 记录结果信息
        if (StringUtils.isNotBlank(stageResult.getMsg())) {
            stageExecutionResultMsgService.create(StageExecutionResultMsg.builder()
                    .stageExecutionId(stageExecution.getId())
                    .msg(stageResult.getMsg())
                    .createTime(new Date()).build());
        }

        // 记录sharedContext
//        TaskExecution taskExecution = taskExecutionServiceWrapper.selectById(stageStartup.getTaskExecutionId());
//        VerifyUtil.shallBeTrue(taskExecution != null,
//                "The task execution record with ID %d does not exist.".formatted(stageStartup.getTaskExecutionId()));
//
//        taskSharedContextService.safeUpdate(taskExecution.getEncodedSharedContextEsId(),
//                TaskSharedContext.builder()
//                        .taskExecutionId(stageStartup.getTaskExecutionId())
//                        .taskSharedContextEncodingSnapshot(stageResult.getEncodedSharedContext())
//                        .updateTime(stageResult.getCompletionTime())
//                        .build()
//        );


        // 最后还是决定以快照的形式记录在每个stage的startup中
        StageStartupParam stageStartupParam = stageStartupParamService.getById(stageStartup.getStartupParamEsId());
        stageStartupParam.setEncodedSharedContextSnapshotAtCompletion(stageResult.getEncodedSharedContext());
        stageStartupParam.setUpdateTime(new Date());
        stageStartupParamService.update(stageStartup.getStartupParamEsId(), stageStartupParam);

    }
}
