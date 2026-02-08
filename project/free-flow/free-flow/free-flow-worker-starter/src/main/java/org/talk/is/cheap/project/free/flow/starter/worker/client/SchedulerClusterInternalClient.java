package org.talk.is.cheap.project.free.flow.starter.worker.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.RegistryWorkerReq;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.starter.worker.config.ClusterAutoConfig;

import java.net.URI;


@FeignClient(
        name = "scheduler-cluster-client",
        url = "None",
        configuration = ClusterAutoConfig.FeignLogLevelConfig.class
)
public interface SchedulerClusterInternalClient {

    String CLIENT_NAME = "scheduler-cluster-client";

    @RequestMapping(path = URIs.SchedulerClusterInternalURIs.ADDRESS, method = RequestMethod.GET)
    @ResponseBody
    HttpBody<String> getSchedulerAddress(URI host);


    @RequestMapping(path = URIs.SchedulerClusterInternalURIs.LEADER, method = RequestMethod.GET)
    @ResponseBody
    HttpBody<String> getLeaderAddress(URI host);


    @RequestMapping(path = URIs.SchedulerClusterInternalURIs.REGISTRY_WORKER, method = RequestMethod.POST)
    @ResponseBody
    HttpBody<String> registryWorker(URI host, @RequestBody RegistryWorkerReq req);
}
