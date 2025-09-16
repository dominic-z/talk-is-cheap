package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime;


import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.message.impl.StartWorkerTaskReq;
import org.talk.is.cheap.project.free.flow.common.task.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务运行时的服务对象，用于提供任务运行时的各种输入、上下文服务
 */
@Service
public class TaskRuntimeService {

    private final Map<Long,StageRuntimeEnv> stageRuntimeEnvMap = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void createTaskRuntimeEnv(TaskDefinitionBO taskDefinitionBO, StartWorkerTaskReq.TaskStartupData taskStartupData) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {


        InputCodec codec = (InputCodec) taskDefinitionBO.getSharedContextCodecClass().getDeclaredConstructor().newInstance();
        TaskRuntimeEnv taskRuntimeEnv = TaskRuntimeEnv.builder()
                .taskStartupId(taskStartupData.getTaskStartupId())
                .sharedContextCodec(codec)
                .encodedSharedContext(taskStartupData.getEncodedSharedContext())
                .sharedContextClass(taskDefinitionBO.getSharedContextClass())
                .taskDefinitionBO(taskDefinitionBO)
                .build();

        Map<String, StageRuntimeEnv> stageRuntimeEnvMap = new HashMap<>();
        for (String stageName : taskStartupData.getStageStartupDataMap().keySet()) {
            StageDefinitionBO stageDefinitionBO = taskDefinitionBO.getStageDefinitionMap().get(stageName);

            StartWorkerTaskReq.StageStartupData stageStartupData = taskStartupData.getStageStartupDataMap().get(stageName);
            InputCodec inputCodec = stageDefinitionBO.getInputCodecClass().getDeclaredConstructor().newInstance();

            StageRuntimeEnv stageRuntimeEnv = StageRuntimeEnv.builder()
                    .stageStartupId(stageStartupData.getStageStartupId())
                    .inputCodec(inputCodec)
                    .encodedInput(stageStartupData.getEncodedInput())
                    .taskRuntimeEnv(taskRuntimeEnv)
                    .inputClass(stageDefinitionBO.getInputClass())
                    .build();

            this.stageRuntimeEnvMap.put(stageRuntimeEnv.getStageStartupId(), stageRuntimeEnv);
        }

    }

    public StageRuntimeEnv getStageRuntimeEnv(long stageStartupId){
        return stageRuntimeEnvMap.get(stageStartupId);
    }
}
