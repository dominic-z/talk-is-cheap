package org.talk.is.cheap.project.free.flow.scheduler.cluster.controller.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.RegistryWorkerReq;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.controller.ClusterInternalController;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.SchedulerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;

@RestController
@RequestMapping
@Slf4j
public class ClusterInternalControllerImpl implements ClusterInternalController {

    @Autowired
    private SchedulerClusterManager schedulerClusterManager;

    @Autowired
    private WorkerClusterManager workerClusterManager;

    @Override
    public HttpBody<String> getSchedulerAddress() {
        return HttpBody.<String>builder().data(schedulerClusterManager.getCurrentSchedulerAddress()).code(0).build();
    }

    @Override
    public HttpBody<String> getLeaderAddress() {
        try {
            return HttpBody.<String>builder().data(schedulerClusterManager.getLeaderAddress()).code(ResultCode.SUCCESS.getCode()).build();
        } catch (Exception e) {
            log.error("error get leader id", e);
            return HttpBody.<String>builder().msg(e.getMessage()).code(ResultCode.FAIL.getCode()).build();
        }
    }

    @Override
    public HttpBody<String> registryWorker(@RequestBody RegistryWorkerReq req) {
        log.info("WorkerRegistryReq {}", req);
        return HttpBody.<String>builder().code(ResultCode.SUCCESS.getCode()).build();
    }

    @Override
    public HttpBody<Void> safeToTerminate(String workerAddr) {
        HttpBody<Void> resp = new HttpBody<>();
        try {
            workerClusterManager.safeToTerminate(workerAddr);
            resp.success();
        } catch (Exception e) {
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;
    }
}
