package org.talk.is.cheap.project.free.flow.scheduler.task.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.WorkerSubmitStageResultReq;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.scheduler.task.service.WorkerTaskDriverService;
import org.talk.is.cheap.project.free.flow.scheduler.task.service.WorkerTaskResultService;

/**
 * 1.用来与Worker进行交互，响应stage的成功或者失败
 * 2.接收外界启动任务的请求
 */
@RestController
@Slf4j
public class TaskProcessController {

    @Autowired
    private WorkerTaskResultService workerTaskResultService;

    @Autowired
    private WorkerTaskDriverService workerTaskDriverService;

    @RequestMapping(path = URIs.SchedulerTaskProcessURIs.RESULTS, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpBody<String> taskResult(@RequestBody WorkerSubmitStageResultReq req) {

        HttpBody<String> resp = new HttpBody<>();

        try {

            for (WorkerSubmitStageResultReq.StageResult stageResult : req.getData().getStageResultList()) {
                workerTaskResultService.saveStageResult(stageResult);
                if (stageResult.getSucceeded()) {
                    workerTaskDriverService.driveNextStageFrom(stageResult.getStageStartupId());
                } else {

                }
            }

            resp.success("");
        } catch (Exception e) {
            log.info("error when record task result", e);
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;


    }
}
