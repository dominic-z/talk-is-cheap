package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime;


import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskReq;
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

    private final Map<Long, TaskRuntimeEnv> taskRuntimeEnvMap = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public TaskRuntimeEnv createTaskRuntimeEnvs(TaskDefinitionBO taskDefinitionBO,
                                                WorkerStartTaskReq.Data workerStartTaskData) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {


        InputCodec codec = (InputCodec) taskDefinitionBO.getSharedContextCodecClass().getDeclaredConstructor().newInstance();
        TaskRuntimeEnv taskRuntimeEnv = TaskRuntimeEnv.builder()
                .taskExecutionId(workerStartTaskData.getTaskExecutionId())
                .sharedContextCodec(codec)
                .encodedSharedContext(workerStartTaskData.getEncodedTaskStartupContext())
                .sharedContextClass(taskDefinitionBO.getSharedContextClass())
                .stageRuntimeEnvs(new HashMap<>())
                .taskDefinitionBO(taskDefinitionBO)
                .build();
        for (WorkerStartTaskReq.StartStageDatum startStageDatum : workerStartTaskData.getStartStageData()) {
            StageDefinitionBO stageDefinitionBO = taskDefinitionBO.getStageDefinitionMap().get(startStageDatum.getStageName());

            VerifyUtil.shallBeTrue(stageDefinitionBO != null,
                    String.format("The corresponding stage definition cannot be found. %s", startStageDatum.getStageName()));

            InputCodec inputCodec = stageDefinitionBO.getInputCodecClass().getDeclaredConstructor().newInstance();

            StageRuntimeEnv stageRuntimeEnv = StageRuntimeEnv.builder()
                    .stageExecutionId(startStageDatum.getStageExecutionId())
                    .inputCodec(inputCodec)
                    .encodedInput(startStageDatum.getEncodedInput())
                    .taskRuntimeEnv(taskRuntimeEnv)
                    .inputClass(stageDefinitionBO.getInputClass())
                    .build();
            taskRuntimeEnv.getStageRuntimeEnvs().put(startStageDatum.getStageName(), stageRuntimeEnv);
        }
        this.taskRuntimeEnvMap.put(workerStartTaskData.getTaskExecutionId(), taskRuntimeEnv);
        return taskRuntimeEnv;

    }


    public TaskRuntimeEnv get(long taskExecutionId){
        return this.taskRuntimeEnvMap.get(taskExecutionId);
    }

    public TaskRuntimeEnv remove(long taskExecutionId){
        return this.taskRuntimeEnvMap.remove(taskExecutionId);
    }

}
