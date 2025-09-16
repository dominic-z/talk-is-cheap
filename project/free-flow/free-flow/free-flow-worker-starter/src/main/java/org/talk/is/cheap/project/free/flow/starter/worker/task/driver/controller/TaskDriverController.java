package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.controller;


import io.vavr.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.StartWorkerTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.StartWorkerTaskResp;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.service.TaskDriverService;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TaskDriverController {

    @Autowired
    private TaskDriverService taskDriverService;

    // todo: 测试
    @RequestMapping(path = URIs.WorkerDriverURIs.START_TASK, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public StartWorkerTaskResp startTask(@RequestBody StartWorkerTaskReq req) {
        StartWorkerTaskResp resp = new StartWorkerTaskResp();

        try {
            Map<Tuple2<String, Integer>, StartWorkerTaskReq.TaskStartupData> startupDataMap = req.getData().getTaskNameVersionParams();
            if (startupDataMap == null || startupDataMap.isEmpty()) {
                resp.success(null);
                return resp;
            }

            Map<Tuple2<String,Integer>,StartWorkerTaskResp.TaskStartResult> taskStartResultMap = new HashMap<>();
            for (Tuple2<String, Integer> taskNameVersion : startupDataMap.keySet()) {
                String name = taskNameVersion._1();
                Integer version = taskNameVersion._2();

                if(!taskDriverService.canStartTask(name,version)){
                    taskStartResultMap.put(taskNameVersion, StartWorkerTaskResp.TaskStartResult.NO_TASK_DEFINITION);
                }else{
                    taskDriverService.startTask(name,version,startupDataMap.get(taskNameVersion));
                }
            }
            StartWorkerTaskResp.Data data = new StartWorkerTaskResp.Data();
            data.setTaskNameVersions(taskStartResultMap);
            resp.success(data);

        } catch (Exception e) {
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;
    }
}
