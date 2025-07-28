package org.talk.is.cheap.project.free.flow.scheduler.cluster.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.common.message.HttpBody;
import org.talk.is.cheap.project.free.common.message.impl.WorkerRegistryReq;

public interface ClusterController {

    @RequestMapping(path = "/scheduler/id", method = RequestMethod.GET)
    @ResponseBody
    HttpBody<String> getSchedulerId();


    @RequestMapping(path = "/scheduler/leader", method = RequestMethod.GET)
    @ResponseBody
    HttpBody<String> getLeaderId();

    @RequestMapping(path = "/scheduler/registry-worker", method = RequestMethod.POST)
    @ResponseBody
    HttpBody<String> registryWorker(@RequestBody WorkerRegistryReq req);

}
