package org.talk.is.cheap.project.free.flow.scheduler.task.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.talk.is.cheap.project.free.flow.common.message.impl.GetWorkerTaskDefinitionResp;
import org.talk.is.cheap.project.free.flow.common.router.URIs;

import java.net.URI;

@FeignClient(name = "worker-task-definition-client", url = "None")
public interface WorkerTaskDefinitionClient {

    @RequestMapping(path = URIs.WorkerDefinitionURIs.GET_TASK_DEFINITION, method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    GetWorkerTaskDefinitionResp getTaskDefinition(URI host);
}
