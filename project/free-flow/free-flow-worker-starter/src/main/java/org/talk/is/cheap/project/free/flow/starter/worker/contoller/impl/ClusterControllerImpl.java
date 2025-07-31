package org.talk.is.cheap.project.free.flow.starter.worker.contoller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.starter.worker.contoller.ClusterController;
import org.talk.is.cheap.project.free.flow.starter.worker.service.ClusterService;


@RequestMapping(path = "/worker")
@RestController
public class ClusterControllerImpl implements ClusterController {

    @Autowired
    private ClusterService clusterService;

    @Override
    public HttpBody<String> ping() {
        return HttpBody.<String>builder().data("pong").code(ResultCode.SUCCESS.getCode()).build();
    }

    @Override
    public HttpBody<String> terminate() {
        HttpBody.HttpBodyBuilder<String> builder = HttpBody.<String>builder();
        try {
            clusterService.terminate();
            return builder.code(ResultCode.SUCCESS.getCode()).build();
        } catch (Exception e) {
            return builder.code(ResultCode.FAIL.getCode()).msg(e.getMessage()).build();
        }
    }
}
