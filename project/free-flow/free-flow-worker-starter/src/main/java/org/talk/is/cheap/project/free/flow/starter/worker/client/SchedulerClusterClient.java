package org.talk.is.cheap.project.free.flow.starter.worker.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.common.message.HttpBody;
import org.talk.is.cheap.project.free.common.message.impl.WorkerRegistryReq;

import java.net.URI;


@FeignClient(
        name = "scheduler-cluster-client",
        url = "None"
)
public interface SchedulerClusterClient {

    String CLIENT_NAME = "scheduler-cluster-client";

    @RequestMapping(path = "/cluster/scheduler/id", method = RequestMethod.GET)
    @ResponseBody
    HttpBody<String> getSchedulerId(URI host);


    @RequestMapping(path = "/cluster/scheduler/leader", method = RequestMethod.GET)
    @ResponseBody
    HttpBody<String> getLeaderId(URI host);


    @RequestMapping(path = "/cluster/scheduler/registry-worker", method = RequestMethod.POST)
    @ResponseBody
    HttpBody<String> registryWorker(URI host, @RequestBody WorkerRegistryReq req);
}
