package org.talk.is.cheap.project.free.flow.starter.worker.task.definition.controller;


import io.vavr.Tuple2;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.GetTaskDefinitionResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.QueryTaskDefinitionReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.QueryTaskDefinitionResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.vo.StageDefinitionVO;
import org.talk.is.cheap.project.free.flow.common.message.impl.vo.TaskDefinitionVO;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service.LocalTaskDefinitionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@Slf4j
public class TaskDefinitionController {

    @Autowired
    private LocalTaskDefinitionService localTaskDefinitionService;


    private final ModelMapper modelMapper = new ModelMapper();

    @PostConstruct
    private void init() {
//        Tuple2
        // 新发现的对象属性拷贝工具，挺不错
        modelMapper.typeMap(TaskDefinitionBO.class, TaskDefinitionVO.class);
    }

    @RequestMapping(path = URIs.WorkerDefinitionURIs.GET_TASK_DEFINITION, method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public GetTaskDefinitionResp getTaskDefinitionResp() {
        GetTaskDefinitionResp resp = new GetTaskDefinitionResp();

        try {
            List<TaskDefinitionVO> taskDefinitionVOS = new ArrayList<>();
            for (String taskName : localTaskDefinitionService.getTaskNames()) {
                TaskDefinitionBO taskDefinitionBO = localTaskDefinitionService.getTaskDefinitionBO(taskName);

                TaskDefinitionVO taskDefinitionVO = modelMapper.map(taskDefinitionBO, TaskDefinitionVO.class);

            }
            resp.success(taskDefinitionVOS);
        } catch (Exception e) {
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;
    }
}
