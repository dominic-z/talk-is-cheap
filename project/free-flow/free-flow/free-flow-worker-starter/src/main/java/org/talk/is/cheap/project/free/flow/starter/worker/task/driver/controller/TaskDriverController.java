package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.ReScheduleTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerResumeTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerResumeTaskResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerRetryStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskResp;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.service.TaskDriverService;

@RestController
@Slf4j
public class TaskDriverController {

    @Autowired
    private TaskDriverService taskDriverService;


    @RequestMapping(path = URIs.WorkerDriverURIs.TASK_START, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public WorkerStartTaskResp startTask(@RequestBody WorkerStartTaskReq req) {
        WorkerStartTaskResp resp = new WorkerStartTaskResp();

        try {
            WorkerStartTaskReq.Data data = req.getData();
            VerifyUtil.requireNotNull(data, "worker start task data is null");
            if (!taskDriverService.canStartTask(data.getTaskName(), data.getTaskVersion())) {
                resp.fail(ResultCode.NO_TASK_DEFINITION,
                        String.format("The task definition for (name:%s,version:%d) is not available.", data.getTaskName(),
                                data.getTaskVersion()));
                return resp;
            }
            taskDriverService.startTask(data);
            resp.success(null);

        } catch (Exception e) {
            log.error("启动任务（stageExeId:{}）失败", req.getData() != null ? req.getData().getTaskExecutionId() : null, e);
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;
    }

    @RequestMapping(path = URIs.WorkerDriverURIs.STAGE_RETRY, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpBody<String> retryStage(@RequestBody WorkerRetryStageReq req) {
        HttpBody<String> resp = new HttpBody<>();
        try {
            WorkerRetryStageReq.WorkerRetryStageReqData data = req.getData();
            VerifyUtil.requireAllNotNull("存在入参为空", data, data.getStageName(), data.getTaskExecutionId(), data.getStageExecutionId());
            taskDriverService.retryStage(data.getTaskExecutionId(), data.getStageName(), data.getStageExecutionId(),
                    data.getEncodedInput());
            resp.success("");
        } catch (Exception e) {
            log.error("重试任务失败", e);
            resp.fail(ResultCode.FAIL, String.format("重试任务失败，%s", e.getMessage()));
        }
        return resp;
    }

    @RequestMapping(path = URIs.WorkerDriverURIs.TASK_CLEAR, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpBody<String> clearTask(@RequestParam("taskExecutionId") long taskExecutionId) {
        HttpBody<String> resp = new HttpBody<>();
        try {
            taskDriverService.clearCompletedTaskObject(taskExecutionId);
            resp.success("");
        } catch (Exception e) {
            log.error("重试任务失败", e);
            resp.fail(ResultCode.FAIL, String.format("清空任务对象失败，%s", e.getMessage()));
        }
        return resp;
    }

    @RequestMapping(path = URIs.WorkerDriverURIs.TASK_RESUME, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public WorkerResumeTaskResp resumeTask(@RequestBody WorkerResumeTaskReq req) {
        WorkerResumeTaskResp resp = new WorkerResumeTaskResp();

        try {

            WorkerResumeTaskReq.Data data = req.getData();
            VerifyUtil.requireNotNull(data, "恢复任务信息为空");
            taskDriverService.resumeTask(data);
            resp.success(null);
        } catch (Exception e) {
            log.error("恢复任务{}失败", req.getData(), e);
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;

    }



}
