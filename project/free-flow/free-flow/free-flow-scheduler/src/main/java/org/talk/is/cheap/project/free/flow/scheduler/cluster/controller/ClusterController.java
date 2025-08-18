package org.talk.is.cheap.project.free.flow.scheduler.cluster.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.RegistryWorkerReq;
import org.talk.is.cheap.project.free.flow.common.router.URIs;

public interface ClusterController {

    @RequestMapping(path = URIs.SchedulerClusterURIs.ID, method = RequestMethod.GET)
    @ResponseBody
    HttpBody<String> getSchedulerId();


    @RequestMapping(path = URIs.SchedulerClusterURIs.LEADER, method = RequestMethod.GET)
    @ResponseBody
    HttpBody<String> getLeaderId();

    @RequestMapping(path = URIs.SchedulerClusterURIs.REGISTRY_WORKER, method = RequestMethod.POST)
    @ResponseBody
    HttpBody<String> registryWorker(@RequestBody RegistryWorkerReq req);

}
