package org.talk.is.cheap.project.free.flow.scheduler.task.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerRetryStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskResp;
import org.talk.is.cheap.project.free.flow.common.router.URIs;

import java.net.URI;

@FeignClient(name = "worker-task-driver-client", url = "None")
public interface WorkerTaskDriverClient {

    @RequestMapping(path = URIs.WorkerDriverURIs.TASK_START, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    WorkerStartTaskResp startTask(URI host, @RequestBody WorkerStartTaskReq req);


    @RequestMapping(path = URIs.WorkerDriverURIs.STAGE_START, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpBody<String> retryStage(URI host, @RequestBody WorkerRetryStageReq req);

}
