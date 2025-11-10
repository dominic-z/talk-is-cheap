package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.StartWorkerStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.StartWorkerStageResp;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.service.TaskDriverService;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TaskDriverController {

    @Autowired
    private TaskDriverService taskDriverService;

    // todo: 测试
    @RequestMapping(path = URIs.WorkerDriverURIs.START_STAGE, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public StartWorkerStageResp startStage(@RequestBody StartWorkerStageReq req) {
        StartWorkerStageResp resp = new StartWorkerStageResp();

        try {
            List<StartWorkerStageReq.StageStartupData> stageStartupDataList = req.getData().getStartupDataList();
            if (stageStartupDataList == null || stageStartupDataList.isEmpty()) {
                resp.success(null);
                return resp;
            }

            List<StartWorkerStageResp.StageStartResult> stageStartResults = new ArrayList<>();
            for (StartWorkerStageReq.StageStartupData stageStartupData : stageStartupDataList) {
                String name = stageStartupData.getTaskName();
                Integer version = stageStartupData.getTaskVersion();
                StartWorkerStageResp.StageStartResult stageStartResult = new StartWorkerStageResp.StageStartResult();
                stageStartResult.setStageStartupId(stageStartupData.getStageStartupId());
                if (!taskDriverService.canStartTask(name, version)) {
                    stageStartResult.setResult(StartWorkerStageResp.StageStartResult.Result.NO_TASK_DEFINITION);
                } else {
                    try {
                        if (taskDriverService.startStage(stageStartupData)) {
                            stageStartResult.setResult(StartWorkerStageResp.StageStartResult.Result.SUCCEEDED);
                        }
                    } catch (Exception e) {
                        stageStartResult.setResult(StartWorkerStageResp.StageStartResult.Result.FAILED);
                        stageStartResult.setMsg(e.getMessage());
                    }
                }
                stageStartResults.add(stageStartResult);
            }
            StartWorkerStageResp.Data data = new StartWorkerStageResp.Data();
            data.setStageStartResultList(stageStartResults);
            resp.success(data);
        } catch (Exception e) {
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;
    }
}
