package org.talk.is.cheap.project.free.flow.scheduler.cluster.controller.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.WorkerRegistryReq;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.controller.ClusterController;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.SchedulerClusterManager;

@RestController
@RequestMapping(path = "/cluster")
@Slf4j
public class ClusterControllerImpl implements ClusterController {

    @Autowired
    private SchedulerClusterManager schedulerClusterManager;

    @Override
    public HttpBody<String> getSchedulerId() {
        return HttpBody.<String>builder().msg(schedulerClusterManager.getSchedulerId()).code(0).build();
    }

    @Override
    public HttpBody<String> getLeaderId() {
        try {
            return HttpBody.<String>builder().data(schedulerClusterManager.getLeaderId()).code(ResultCode.SUCCESS.getCode()).build();
        } catch (Exception e) {
            log.error("error get leader id", e);
            return HttpBody.<String>builder().msg(e.getMessage()).code(ResultCode.FAIL.getCode()).build();
        }
    }

    @Override
    public HttpBody<String> registryWorker(@RequestBody WorkerRegistryReq req) {
        log.info("WorkerRegistryReq {}", req);
        return HttpBody.<String>builder().code(ResultCode.SUCCESS.getCode()).build();
    }
}
