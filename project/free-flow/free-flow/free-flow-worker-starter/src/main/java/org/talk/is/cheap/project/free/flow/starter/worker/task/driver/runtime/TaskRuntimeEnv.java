package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.talk.is.cheap.project.free.flow.common.task.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.common.task.codec.JsonInputCodec;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;

import java.util.Map;

/**
 * 用来存储task运行过程中的环境变量等信息
 *
 * @param <T>
 */
@Builder
@Getter
public class TaskRuntimeEnv<T> {

    private final Long taskExecutionId;
    private final InputCodec<T> sharedContextCodec;
    private final Class<T> sharedContextClass;
    private final String encodedSharedContext;
    @Getter(AccessLevel.NONE)
    private T sharedContext;

    private Map<String, String> stageEncodedInputs;

    private Map<String, StageRuntimeEnv<?>> stageRuntimeEnvs;

    @Getter(AccessLevel.NONE)
    private TaskDefinitionBO taskDefinitionBO;

    public T getSharedContext() {
        if (StringUtils.isBlank(encodedSharedContext)) {
            return null;
        }

        if (sharedContext == null) {
            sharedContext = sharedContextCodec.decode(encodedSharedContext, sharedContextClass);
        }
        return sharedContext;
    }

    public String getEncodedSharedContext() {
        if (sharedContext != null) {
            return sharedContextCodec.encode(sharedContext);
        }
        return null;
    }

    public String getTaskName() {
        return this.taskDefinitionBO.getName();
    }

    @Data
    public static class TestParam {
        private String name;
    }

    public static void main(String[] args) {
        Class testParamClass = TestParam.class;
        TaskRuntimeEnv taskRuntimeEnv = TaskRuntimeEnv.builder()
                .taskExecutionId(1L)
                .sharedContextClass(testParamClass)
                .sharedContextCodec(new JsonInputCodec<TestParam>())
                .encodedSharedContext("""
                                {
                          "name": "name_a4e56a2e1cdf"
                        }
                                """)
                .build();
        TestParam s = (TestParam) taskRuntimeEnv.getSharedContext();
        System.out.println(s);
    }
}
