package org.talk.is.cheap.project.free.flow.scheduler.task.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.StartWorkerStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.StartWorkerStageResp;
import org.talk.is.cheap.project.free.flow.common.router.URIs;

import java.net.URI;

@FeignClient(name = "worker-task-driver-client", url = "None")
public interface WorkerTaskDriverClient {

    @RequestMapping(path = URIs.WorkerDriverURIs.START_STAGE, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    StartWorkerStageResp startStage(URI host,@RequestBody StartWorkerStageReq req);
}
