package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskReq;
import org.talk.is.cheap.project.free.flow.common.task.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.StageExecutionBizLogService;
import org.talk.is.cheap.project.free.flow.starter.worker.domain.dto.CreateTaskRuntimeEnvDTO;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service.LocalTaskDefinitionService;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务运行时的服务对象，用于提供任务运行时的各种输入、上下文服务
 */
@Service
public class TaskRuntimeService {

    @Autowired
    private LocalTaskDefinitionService localTaskDefinitionService;
    @Autowired
    private StageExecutionBizLogService stageExecutionBizLogService;
    private final Map<Long, TaskRuntimeEnv<?>> taskExeIdRuntimeEnvMap = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public TaskRuntimeEnv createTaskRuntimeEnv(TaskDefinitionBO taskDefinitionBO,
                                               CreateTaskRuntimeEnvDTO dto) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {


        InputCodec codec = (InputCodec) taskDefinitionBO.getSharedContextCodecClass().getDeclaredConstructor().newInstance();
        Map<String, String> stageEncodedInputs = dto.getStageEncodedInputs();

        Date deadline = null;
        Calendar cal = Calendar.getInstance();
        Date current = cal.getTime();
        if (taskDefinitionBO.getTimeoutInSecond() > 0) {
            cal.add(Calendar.SECOND, taskDefinitionBO.getTimeoutInSecond());
            deadline = cal.getTime();
        }
        TaskRuntimeEnv taskRuntimeEnv = TaskRuntimeEnv.builder()
                .taskExecutionId(dto.getTaskExecutionId())
                .sharedContextCodec(codec)
                .sharedContextClass(taskDefinitionBO.getSharedContextClass())
                .encodedSharedContext(dto.getInitialEncodedSharedContext())
                .startTime(current)
                .deadline(deadline)
                .runtimeEnvStatus(RuntimeEnvStatus.RUNNING)
                .stageEncodedInputs(stageEncodedInputs) // 初始情况选这个属性会存储所有的stage的入参
                .stageRuntimeEnvs(new ConcurrentHashMap<>()) // 这个可能会并发
                .taskDefinitionBO(taskDefinitionBO)
                .taskFailedCount(dto.getTaskFailedCount())
                .build();

        if (dto.getSucceedStageNames() != null && dto.getSucceedStageNames().isEmpty()) {
            taskRuntimeEnv.getSucceedStages().addAll(dto.getSucceedStageNames());
            taskRuntimeEnv.getDispatchedStages().addAll(dto.getSucceedStageNames());
        }

        // 使用懒加载的方式创建，一开始只创建starting stage的stageRuntimeEnv，因为这时候只有这些stage有stageExecutionId
        for (Map.Entry<String, Long> entry : dto.getStartingStageExecutionId().entrySet()) {

            String stageName = entry.getKey();
            Long stageExecutionId = entry.getValue();
            StageDefinitionBO stageDefinitionBO = taskDefinitionBO.getStageDefinitionMap().get(stageName);

            VerifyUtil.requireTrue(stageDefinitionBO != null,
                    String.format("The corresponding stage definition cannot be found. %s", stageName));

            InputCodec inputCodec = stageDefinitionBO.getInputCodecClass().getDeclaredConstructor().newInstance();

            String encodedInput = null;
            if (stageEncodedInputs != null) {
                encodedInput = stageEncodedInputs.get(stageName);
            }
            Calendar stageCal = Calendar.getInstance();
            Date stageStartTime = stageCal.getTime();
            Date stageDeadline = null;
            if (stageDefinitionBO.getTimeout() > 0) {
                cal.add(Calendar.SECOND, stageDefinitionBO.getTimeout());
                stageDeadline = stageCal.getTime();
            }
            StageRuntimeEnv stageRuntimeEnv = StageRuntimeEnv.builder()
                    .stageExecutionId(stageExecutionId)
                    .inputCodec(inputCodec)
                    .encodedInput(encodedInput)
                    .startTime(stageStartTime)
                    .deadline(stageDeadline)
                    .taskRuntimeEnv(taskRuntimeEnv)
                    .inputClass(stageDefinitionBO.getInputClass())
                    .stageExecutionBizLogService(stageExecutionBizLogService)
                    .stageFailedCount(dto.getStageFailedCount() != null ? dto.getStageFailedCount().getOrDefault(stageName, 0) : 0)
                    .build();
            taskRuntimeEnv.getStageRuntimeEnvs().put(stageName, stageRuntimeEnv);
        }
        this.taskExeIdRuntimeEnvMap.put(dto.getTaskExecutionId(), taskRuntimeEnv);
        return taskRuntimeEnv;

    }


    public TaskRuntimeEnv<?> get(long taskExecutionId) {
        return this.taskExeIdRuntimeEnvMap.get(taskExecutionId);
    }

    public TaskRuntimeEnv<?> remove(long taskExecutionId) {
        return this.taskExeIdRuntimeEnvMap.remove(taskExecutionId);
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
                .stageExecutionBizLogService(stageExecutionBizLogService)
                .stageFailedCount(0)
                .build();
        taskRuntimeEnv.getStageRuntimeEnvs().put(stageName, stageRuntimeEnv);
        return stageRuntimeEnv;

    }

    // 重新执行taskExecutionId下的retryStageName的之前，需要更新一个新的stageRuntimeEnv
    public StageRuntimeEnv updateRetryStageRuntimeEnv(long taskExecutionId, long retryStageExecutionId, String retryStageName,
                                                      String encodedInput, Integer stageFailedCount) {
        TaskRuntimeEnv<?> taskRuntimeEnv = this.get(taskExecutionId);
        VerifyUtil.requireNotNull(taskRuntimeEnv, String.format("taskExeId:%d的任务运行环境变量已经丢失", taskExecutionId));

        StageRuntimeEnv failedStageRuntimeEnv = taskRuntimeEnv.getStageRuntimeEnvs().get(retryStageName);
        VerifyUtil.requireNotNull(failedStageRuntimeEnv, String.format("taskExeId:%d中stageName:%s的阶段运行环境变量已经丢失", taskExecutionId,
                retryStageName));

        taskRuntimeEnv.getStageEncodedInputs().put(retryStageName, encodedInput);

        final StageRuntimeEnv retryStageRuntimeEnv = StageRuntimeEnv.builder()
                .taskRuntimeEnv(taskRuntimeEnv)
                .inputClass(failedStageRuntimeEnv.getInputClass())
                .inputCodec(failedStageRuntimeEnv.getInputCodec())
                .encodedInput(encodedInput)
                .stageExecutionId(retryStageExecutionId)
                .stageFailedCount(failedStageRuntimeEnv.getStageFailedCount() + 1) // 重试stage，失败次数+1
                .build();

        taskRuntimeEnv.updateStageRuntimeEnv(retryStageName, retryStageRuntimeEnv);
        return retryStageRuntimeEnv;
    }


}
