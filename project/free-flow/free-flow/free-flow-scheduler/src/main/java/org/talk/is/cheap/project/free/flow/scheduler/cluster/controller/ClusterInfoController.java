package org.talk.is.cheap.project.free.flow.scheduler.cluster.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.QueryClusterInfoReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.QueryClusterInfoResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.RegistryWorkerReq;
import org.talk.is.cheap.project.free.flow.common.router.URIs;

/**
 * 面向前端的cluster
 */
public interface ClusterInfoController {

    @RequestMapping(path = URIs.SchedulerClusterURIs.NODES, method = RequestMethod.POST)
    @ResponseBody
    QueryClusterInfoResp queryNodes(@RequestBody QueryClusterInfoReq req);



}
