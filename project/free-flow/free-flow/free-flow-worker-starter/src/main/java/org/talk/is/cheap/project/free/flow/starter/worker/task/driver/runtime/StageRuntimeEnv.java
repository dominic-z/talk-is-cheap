package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.talk.is.cheap.project.free.flow.common.task.codec.InputCodec;

@Builder
@Getter
public class StageRuntimeEnv<T> {

    private final Long stageStartupId;
    private final InputCodec<T> inputCodec;
    private final Class<T> inputClass;
    private final String encodedInput;
    @Getter(AccessLevel.NONE)
    private T input;
    // 当前stage所在的task的runtime
    @Getter(AccessLevel.NONE)
    private TaskRuntimeEnv taskRuntimeEnv;

    public T getInput() {
        if (input == null) {
            input = inputCodec.decode(encodedInput, inputClass);
        }
        return input;
    }


    public <K> K getSharedContext(){
        return (K) taskRuntimeEnv.getSharedContext();
    }
}
