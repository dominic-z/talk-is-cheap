package org.talk.is.cheap.project.free.flow.scheduler.task.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talk.is.cheap.project.free.flow.common.enums.TaskStageStatus;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerCompleteStageResultReq;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.repository.config.RepositoryAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.ESPojoDTO;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageExecutionResultMsg;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageStartupParam;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageExecutionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.StageExecutionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.StageStartupServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskExecutionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.StageExecutionResultMsgService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.StageStartupParamService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.TaskSharedContextService;

import java.io.IOException;
import java.util.Date;

@Service
@Slf4j
public class WorkerTaskResultService {

    @Autowired
    private StageStartupServiceWrapper stageStartupServiceWrapper;
    @Autowired
    private StageExecutionService stageExecutionService;
    private StageExecutionServiceWrapper stageExecutionServiceWrapper;

    @Autowired
    private StageExecutionResultMsgService stageExecutionResultMsgService;
    @Autowired
    private TaskExecutionServiceWrapper taskExecutionServiceWrapper;
    @Autowired
    private TaskSharedContextService taskSharedContextService;
    @Autowired
    private StageStartupParamService stageStartupParamService;

    /**
     * 某个stage已经完成，记录shareContext快照，正常情况下不需要考虑并发，因为一般情况下一个stage只会有一个scheduler在操作
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = RepositoryAutoConfig.TRANSACTION_MANAGER_BEAN_NAME)
    public void saveStageResult(WorkerCompleteStageResultReq.StageResult stageResult) throws IOException {

        Long stageExecutionId = stageResult.getStageExecutionId();
        StageExecution stageExecution = stageExecutionServiceWrapper.selectById(stageExecutionId, TaskStageStatus.RUNNING.getStatus());
        VerifyUtil.requireTrue(stageExecution != null,
                "The running execution record for the stage with ID %d does not exist.".formatted(stageExecutionId));

        // 更新stage startup
        StageStartup stageStartup = stageStartupServiceWrapper.selectById(stageExecution.getStageStartupId(),
                TaskStageStatus.RUNNING.getStatus());
        VerifyUtil.requireTrue(stageStartup != null,
                "The startup record for the stage with ID %d does not exist.".formatted(stageExecution.getStageStartupId()));
        Integer taskStageStatus = stageResult.getSucceeded() ? TaskStageStatus.SUCCEEDED.getStatus() :
                TaskStageStatus.FAILED.getStatus();

        VerifyUtil.requireTrue(stageExecutionServiceWrapper.updateSelectiveById(
                        stageExecutionId,  new StageExecution().withStatus(taskStageStatus),stageExecution.getRevision()) > 0,
                "更新stageExecution状态失败");
        VerifyUtil.requireTrue(stageStartupServiceWrapper.updateSelectiveById(stageStartup.getId(),
                        new StageStartup().withStatus(taskStageStatus).withCompletionTime(stageResult.getCompletionTime()), stageStartup.getRevision()) > 0,
                "更新stageStartup失败");

        // 记录结果信息
        if (StringUtils.isNotBlank(stageResult.getMsg())) {
            stageExecutionResultMsgService.create(StageExecutionResultMsg.builder()
                    .stageExecutionId(stageExecution.getId())
                    .msg(stageResult.getMsg())
                    .createTime(new Date()).build());
        }

        // 记录sharedContext
        ESPojoDTO<StageStartupParam> esPojoDTO = stageStartupParamService.getByStageStartupId(stageStartup.getId());
        StageStartupParam stageStartupParam = esPojoDTO.getData();
        stageStartupParam.setEncodedSharedContextSnapshotAtCompletion(stageResult.getEncodedSharedContextAtCompletion());
        stageStartupParam.setUpdateTime(new Date());
        stageStartupParamService.update(esPojoDTO.getId(), stageStartupParam);

    }
}
