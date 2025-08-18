package org.talk.is.cheap.project.free.flow.starter.worker.cluster.contoller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.router.URIs;


/**
 * 用于对外提供本worker的状态心跳、流转的controller
 */
public interface ClusterController {

    @GetMapping(path = URIs.WorkerClusterURIs.PING)
    @ResponseBody
    HttpBody<String> ping();

    @PostMapping(path = URIs.WorkerClusterURIs.TERMINATE)
    @ResponseBody
    HttpBody<String> terminate();
}
