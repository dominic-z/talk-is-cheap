package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime;


import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.message.impl.StartWorkerStageReq;
import org.talk.is.cheap.project.free.flow.common.task.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务运行时的服务对象，用于提供任务运行时的各种输入、上下文服务
 */
@Service
public class TaskRuntimeService {

    private final Map<Long, StageRuntimeEnv> stageRuntimeEnvMap = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public StageRuntimeEnv createStageRuntimeEnv(TaskDefinitionBO taskDefinitionBO, StartWorkerStageReq.StageStartupData stageStartupData) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {


        InputCodec codec = (InputCodec) taskDefinitionBO.getSharedContextCodecClass().getDeclaredConstructor().newInstance();
        TaskRuntimeEnv taskRuntimeEnv = TaskRuntimeEnv.builder()
                .taskStartupId(stageStartupData.getTaskStartupId())
                .sharedContextCodec(codec)
                .encodedSharedContext(stageStartupData.getEncodedSharedContext())
                .sharedContextClass(taskDefinitionBO.getSharedContextClass())
                .taskDefinitionBO(taskDefinitionBO)
                .build();

        Map<String, StageRuntimeEnv> stageRuntimeEnvMap = new HashMap<>();
        StageDefinitionBO stageDefinitionBO = taskDefinitionBO.getStageDefinitionMap().get(stageStartupData.getStageName());
        VerifyUtil.shallBeTrue(stageDefinitionBO != null,
                String.format("The corresponding stage definition cannot be found. %s", stageStartupData.getStageName()));

        InputCodec inputCodec = stageDefinitionBO.getInputCodecClass().getDeclaredConstructor().newInstance();

        StageRuntimeEnv stageRuntimeEnv = StageRuntimeEnv.builder()
                .stageStartupId(stageStartupData.getStageStartupId())
                .inputCodec(inputCodec)
                .encodedInput(stageStartupData.getEncodedInput())
                .taskRuntimeEnv(taskRuntimeEnv)
                .inputClass(stageDefinitionBO.getInputClass())
                .build();

        this.stageRuntimeEnvMap.put(stageRuntimeEnv.getStageStartupId(), stageRuntimeEnv);
        return stageRuntimeEnv;

    }

    public StageRuntimeEnv getStageRuntimeEnv(long stageStartupId) {
        return stageRuntimeEnvMap.get(stageStartupId);
    }
}
