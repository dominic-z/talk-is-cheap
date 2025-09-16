package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
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

    private final Long taskStartupId;
    private final InputCodec<T> sharedContextCodec;
    private final Class<T> sharedContextClass;
    private final String encodedSharedContext;
    @Getter(AccessLevel.NONE)
    private T sharedContext;

    @Getter(AccessLevel.NONE)
    private TaskDefinitionBO taskDefinitionBO;

    public T getSharedContext() {
        if (sharedContext == null) {
            sharedContext = sharedContextCodec.decode(encodedSharedContext, sharedContextClass);
        }
        return sharedContext;
    }

    @Data
    public static class TestParam {
        private String name;
    }

    public static void main(String[] args) {
        Class testParamClass = TestParam.class;
        TaskRuntimeEnv taskRuntimeEnv = TaskRuntimeEnv.builder()
                .taskStartupId(1L)
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
