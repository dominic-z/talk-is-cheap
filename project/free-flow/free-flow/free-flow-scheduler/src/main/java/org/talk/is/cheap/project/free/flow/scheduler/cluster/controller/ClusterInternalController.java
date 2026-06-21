package org.talk.is.cheap.project.free.flow.scheduler.cluster.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.RegistryWorkerReq;
import org.talk.is.cheap.project.free.flow.common.router.URIs;

/**
 * 面向集群内的controller
 */
public interface ClusterInternalController {

    @RequestMapping(path = URIs.SchedulerClusterInternalURIs.ADDRESS, method = RequestMethod.GET)
    @ResponseBody
    HttpBody<String> getSchedulerAddress();


    @RequestMapping(path = URIs.SchedulerClusterInternalURIs.LEADER, method = RequestMethod.GET)
    @ResponseBody
    HttpBody<String> getLeaderAddress();

    @RequestMapping(path = URIs.SchedulerClusterInternalURIs.REGISTRY_WORKER, method = RequestMethod.POST)
    @ResponseBody
    HttpBody<String> registryWorker(@RequestBody RegistryWorkerReq req);


    @RequestMapping(path = URIs.SchedulerClusterInternalURIs.SAFE_TO_TERMINATE, method = RequestMethod.GET)
    @ResponseBody
    HttpBody<Void> safeToTerminate(@RequestParam("workerAddr") String workerAddr);

}
