package org.talk.is.cheap.project.free.flow.scheduler.task.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerResumeTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerResumeTaskResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerRetryStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskResp;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.scheduler.config.FeignReactorConfig;

import java.net.URI;


/**
 * feign打印日志，这个levale只会控制日志的内容
 * https://www.qianwen.com/share/chat/d865ec1f74e849978ffd8a25027e3aee
 */
@FeignClient(name = "worker-task-driver-client", url = "None", configuration = FeignReactorConfig.FeignLogLevelConfig.class)
public interface WorkerTaskDriverClient {

    @RequestMapping(path = URIs.WorkerDriverURIs.TASK_START, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    WorkerStartTaskResp startTask(URI host, @RequestBody WorkerStartTaskReq req);


    @RequestMapping(path = URIs.WorkerDriverURIs.STAGE_RETRY, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpBody<String> retryStage(URI host, @RequestBody WorkerRetryStageReq req);


    @RequestMapping(path = URIs.WorkerDriverURIs.TASK_CLEAR, method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpBody<String> clearTask(URI host, @RequestParam("taskExecutionId") long taskExecutionId);

    @RequestMapping(path = URIs.WorkerDriverURIs.TASK_RESUME, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    WorkerResumeTaskResp resumeTask(URI host, @RequestBody WorkerResumeTaskReq req);
}
