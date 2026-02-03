package org.talk.is.cheap.project.free.flow.starter.worker.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.PrepareStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.PrepareStageResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerCompleteStageResultReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerFailStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerFailStageResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerStartStageReportReq;
import org.talk.is.cheap.project.free.flow.common.router.URIs;

import java.net.URI;


@FeignClient(
        name = "scheduler-task-process-client",
        url = "None"
)
public interface SchedulerTaskProcessClient {

    String CLIENT_NAME = "scheduler-task-process-client";

    @RequestMapping(path = URIs.SchedulerTaskProcessURIs.STAGE_START_REPORT, method = RequestMethod.POST, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpBody<String> stageStartReport(URI host, @RequestBody WorkerStartStageReportReq req);

    @RequestMapping(path = URIs.SchedulerTaskProcessURIs.STAGE_COMPLETE, method = RequestMethod.POST, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpBody<String> completeStage(URI host, @RequestBody WorkerCompleteStageResultReq req);

    @RequestMapping(path = URIs.SchedulerTaskProcessURIs.STAGE_PREPARE, method = RequestMethod.POST, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    PrepareStageResp prepareStage(URI host, @RequestBody PrepareStageReq req);


    @RequestMapping(path = URIs.SchedulerTaskProcessURIs.STAGE_FAIL, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    WorkerFailStageResp failStage(URI host, @RequestBody WorkerFailStageReq req);
}
