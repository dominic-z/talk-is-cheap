package org.talk.is.cheap.project.free.flow.starter.worker.task.definition.controller;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.enums.StageType;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.GetWorkerTaskDefinitionResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.StageDefinitionDTO;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.TaskDefinitionDTO;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service.LocalTaskDefinitionService;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class TaskDefinitionController {

    @Autowired
    private LocalTaskDefinitionService localTaskDefinitionService;


    private final ModelMapper modelMapper = new ModelMapper();

    @PostConstruct
    private void init() {
        // 新发现的对象属性拷贝工具，挺不错
        final AbstractConverter<Class<?>, String> classNameConverter = new AbstractConverter<>() {
            @Override
            protected String convert(Class<?> source) {
                return source.getName();
            }
        };
        modelMapper.typeMap(TaskDefinitionBO.class, TaskDefinitionDTO.class)
                .addMappings(mapper -> {
                    mapper.using(classNameConverter)
                            .map(TaskDefinitionBO::getSharedContextClass, TaskDefinitionDTO::setSharedContextFullyQualifiedClassName);
                    mapper.using(classNameConverter)
                            .map(TaskDefinitionBO::getSharedContextCodecClass, TaskDefinitionDTO::setSharedContextCodecFullyQualifiedClassName);
                });

        modelMapper.typeMap(StageDefinitionBO.class, StageDefinitionDTO.class)
                .addMappings(mapper -> {
                    mapper.using(classNameConverter)
                            .map(StageDefinitionBO::getInputClass, StageDefinitionDTO::setInputFullyQualifiedClassName);
                    mapper.using(classNameConverter)
                            .map(StageDefinitionBO::getInputCodecClass, StageDefinitionDTO::setInputCodecFullyQualifiedClassName);
                    mapper.using(new AbstractConverter<StageType, Integer>() {
                        @Override
                        protected Integer convert(StageType source) {
                            return source.getType();
                        }
                    }).map(StageDefinitionBO::getStageType,StageDefinitionDTO::setStageType);
                });
    }

    @RequestMapping(path = URIs.WorkerDefinitionURIs.GET_TASK_DEFINITION, method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public GetWorkerTaskDefinitionResp getTaskDefinition() {
        GetWorkerTaskDefinitionResp resp = new GetWorkerTaskDefinitionResp();

        try {
            List<TaskDefinitionDTO> taskDefinitionDTOS = new ArrayList<>();
            for (String taskName : localTaskDefinitionService.getTaskNames()) {
                TaskDefinitionBO taskDefinitionBO = localTaskDefinitionService.getTaskDefinitionBO(taskName);

                TaskDefinitionDTO taskDefinitionDTO = modelMapper.map(taskDefinitionBO, TaskDefinitionDTO.class);
                taskDefinitionDTOS.add(taskDefinitionDTO);
            }

            resp.success(taskDefinitionDTOS);
        } catch (Exception e) {
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;
    }
}
