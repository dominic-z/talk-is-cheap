package org.talk.is.cheap.project.free.flow.starter.worker.contoller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.common.message.HttpBody;
import org.talk.is.cheap.project.free.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.starter.worker.contoller.ClusterController;
import org.talk.is.cheap.project.free.flow.starter.worker.service.ClusterService;


@RequestMapping(path = "/worker")
@RestController
public class ClusterControllerImpl implements ClusterController {

    @Autowired
    private ClusterService clusterService;

    @Override
    public HttpBody<String> ping() {
        clusterService.ping();
        return HttpBody.<String>builder().data("pong").code(ResultCode.SUCCESS.getCode()).build();
    }
}
