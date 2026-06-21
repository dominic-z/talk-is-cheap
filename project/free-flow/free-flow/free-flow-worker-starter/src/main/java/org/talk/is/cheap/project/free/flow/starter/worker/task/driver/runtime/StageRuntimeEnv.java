package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.talk.is.cheap.project.free.flow.common.task.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.StageExecutionBizLogService;

import java.util.Calendar;
import java.util.Date;

@Builder
@Getter
@ToString
public class StageRuntimeEnv<T> {

    private Long stageExecutionId;
    private final InputCodec<T> inputCodec;
    private final Class<T> inputClass;
    private final String encodedInput;
    @Setter
    private Date startTime;
    private final Integer stageFailedCount;
    private final StageExecutionBizLogService stageExecutionBizLogService;
    @Getter(AccessLevel.NONE)
    private T input;
    // 当前stage所在的task的runtime
    @Getter(AccessLevel.NONE)
    private final TaskRuntimeEnv<?> taskRuntimeEnv;

    @Getter(AccessLevel.NONE)
    private final StageDefinitionBO stageDefinitionBO;

    public T getInput() {
        if (StringUtils.isBlank(encodedInput)) {
            return null;
        }
        if (input == null) {
            input = inputCodec.decode(encodedInput, inputClass);
        }
        return input;
    }


    @SuppressWarnings("unchecked")
    public <K> K getSharedContext() {
        return (K) taskRuntimeEnv.getSharedContext();
    }


    public void log(String s) {
        stageExecutionBizLogService.logAsync(taskRuntimeEnv.getTaskExecutionId(), stageExecutionId,
                String.format("[%s]-[%s]-%s", taskRuntimeEnv.getTaskExecutionId(), stageExecutionId, s));
    }

    public Date getDeadline() {
        if (stageDefinitionBO.getTimeout() <= 0) {
            return null;
        }
        Calendar instance = Calendar.getInstance();
        instance.setTime(this.startTime);
        instance.add(Calendar.SECOND, stageDefinitionBO.getTimeout());
        return instance.getTime();
    }
}
