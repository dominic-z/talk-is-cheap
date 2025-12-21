package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskReq;
import org.talk.is.cheap.project.free.flow.common.task.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service.LocalTaskDefinitionService;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 任务运行时的服务对象，用于提供任务运行时的各种输入、上下文服务
 */
@Service
public class TaskRuntimeService {

    @Autowired
    private LocalTaskDefinitionService localTaskDefinitionService;
    private final Map<Long, TaskRuntimeEnv<?>> taskRuntimeEnvMap = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public TaskRuntimeEnv createTaskRuntimeEnvs(TaskDefinitionBO taskDefinitionBO,
                                                WorkerStartTaskReq.Data workerStartTaskData) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {


        InputCodec codec = (InputCodec) taskDefinitionBO.getSharedContextCodecClass().getDeclaredConstructor().newInstance();
        Map<String, String> stageEncodedInputs = workerStartTaskData.getStageEncodedInputs();
        TaskRuntimeEnv taskRuntimeEnv = TaskRuntimeEnv.builder()
                .taskExecutionId(workerStartTaskData.getTaskExecutionId())
                .sharedContextCodec(codec)
                .encodedSharedContext(workerStartTaskData.getEncodedTaskStartupContext())
                .sharedContextClass(taskDefinitionBO.getSharedContextClass())
                .stageEncodedInputs(stageEncodedInputs)
                .stageRuntimeEnvs(new ConcurrentHashMap<>()) // 这个可能会并发
                .taskDefinitionBO(taskDefinitionBO)
                .build();

        // 使用懒加载的方式创建，一开始只创建starting stage的stageRuntimeEnv，因为这时候只有这些stage有stageExecutionId
        for (Map.Entry<String, Long> entry : workerStartTaskData.getStartingStageExecutionId().entrySet()) {

            String stageName = entry.getKey();
            Long stageExecutionId = entry.getValue();
            StageDefinitionBO stageDefinitionBO = taskDefinitionBO.getStageDefinitionMap().get(stageName);

            VerifyUtil.shallBeTrue(stageDefinitionBO != null,
                    String.format("The corresponding stage definition cannot be found. %s", stageName));

            InputCodec inputCodec = stageDefinitionBO.getInputCodecClass().getDeclaredConstructor().newInstance();

            String encodedInput = null;
            if (stageEncodedInputs != null) {
                encodedInput = stageEncodedInputs.get(stageName);
            }
            StageRuntimeEnv stageRuntimeEnv = StageRuntimeEnv.builder()
                    .stageExecutionId(stageExecutionId)
                    .inputCodec(inputCodec)
                    .encodedInput(encodedInput)
                    .taskRuntimeEnv(taskRuntimeEnv)
                    .inputClass(stageDefinitionBO.getInputClass())
                    .build();
            taskRuntimeEnv.getStageRuntimeEnvs().put(stageName, stageRuntimeEnv);
        }
        this.taskRuntimeEnvMap.put(workerStartTaskData.getTaskExecutionId(), taskRuntimeEnv);
        return taskRuntimeEnv;

    }


    public TaskRuntimeEnv<?> get(long taskExecutionId) {
        return this.taskRuntimeEnvMap.get(taskExecutionId);
    }

    public TaskRuntimeEnv<?> remove(long taskExecutionId) {
        return this.taskRuntimeEnvMap.remove(taskExecutionId);
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public StageRuntimeEnv<?> createStageRuntimeEnv(TaskRuntimeEnv<?> taskRuntimeEnv, String stageName, long stageExecutionId) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (taskRuntimeEnv.getStageRuntimeEnvs().containsKey(stageName)) {
            return taskRuntimeEnv.getStageRuntimeEnvs().get(stageName);
        }

        StageDefinitionBO stageDefinitionBO =
                localTaskDefinitionService.getTaskDefinitionBO(taskRuntimeEnv.getTaskName()).getStageDefinitionMap().get(stageName);
        InputCodec inputCodec = stageDefinitionBO.getInputCodecClass().getDeclaredConstructor().newInstance();
        StageRuntimeEnv stageRuntimeEnv = StageRuntimeEnv.builder()
                .stageExecutionId(stageExecutionId)
                .inputCodec(inputCodec)
                .encodedInput(taskRuntimeEnv.getStageEncodedInputs().get(stageName))
                .taskRuntimeEnv(taskRuntimeEnv)
                .inputClass(stageDefinitionBO.getInputClass())
                .build();
        taskRuntimeEnv.getStageRuntimeEnvs().put(stageName, stageRuntimeEnv);
        return stageRuntimeEnv;

    }


}
