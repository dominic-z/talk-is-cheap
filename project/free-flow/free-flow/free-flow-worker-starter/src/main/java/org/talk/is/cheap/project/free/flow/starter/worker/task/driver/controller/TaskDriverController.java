package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskResp;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.service.TaskDriverService;

@RestController
public class TaskDriverController {

    @Autowired
    private TaskDriverService taskDriverService;


    @RequestMapping(path = URIs.WorkerDriverURIs.TASK_START, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public WorkerStartTaskResp startTask(@RequestBody WorkerStartTaskReq req) {
        WorkerStartTaskResp resp = new WorkerStartTaskResp();

        try {
            WorkerStartTaskReq.Data data = req.getData();
            VerifyUtil.shallNotBeNull(data, "worker start task data is null");
            if (!taskDriverService.canStartTask(data.getTaskName(), data.getTaskVersion())) {
                resp.fail(ResultCode.NO_TASK_DEFINITION,
                        String.format("The task definition for (name:%s,version:%d) is not available.", data.getTaskName(),
                                data.getTaskVersion()));
                return resp;
            }
            taskDriverService.startTask(data);

        } catch (Exception e) {
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;
    }
}
