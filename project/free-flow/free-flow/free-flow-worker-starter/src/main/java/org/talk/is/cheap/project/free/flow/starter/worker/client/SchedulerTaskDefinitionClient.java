package org.talk.is.cheap.project.free.flow.starter.worker.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.QueryTaskDefinitionDetailsReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.QueryTaskDefinitionDetailResp;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.starter.worker.config.ClusterAutoConfig;

import java.net.URI;


@FeignClient(
        name = "scheduler-task-definition-client",
        url = "None",
        configuration = ClusterAutoConfig.FeignLogLevelConfig.class

)
public interface SchedulerTaskDefinitionClient {

    String CLIENT_NAME = "scheduler-task-definition-client";

    @RequestMapping(path = URIs.SchedulerTaskDefinitionURIs.DETAILS_QUERY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    QueryTaskDefinitionDetailResp queryTaskDefinition(URI host, @RequestBody QueryTaskDefinitionDetailsReq req);


}
