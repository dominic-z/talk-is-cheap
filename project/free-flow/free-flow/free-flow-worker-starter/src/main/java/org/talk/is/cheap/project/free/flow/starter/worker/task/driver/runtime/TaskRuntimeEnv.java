package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime;

import cn.hutool.core.collection.ConcurrentHashSet;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.talk.is.cheap.project.free.flow.common.task.codec.InputCodec;
import org.talk.is.cheap.project.free.flow.common.task.codec.JsonInputCodec;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Date startTime;
    private final Date deadline;// 超时截止时间
    private final Integer taskFailedCount;
    @Getter(AccessLevel.NONE)
    private T sharedContext;

    @Setter
    private RuntimeEnvStatus runtimeEnvStatus;

    private Map<String, String> stageEncodedInputs;

    private Map<String, StageRuntimeEnv<?>> stageRuntimeEnvs;

    // 已经完成运行的stage
    private Set<String> succeedStages;
    // 已经失败的stage
    private Set<String> failedStages;
    // 存储那些已经要执行或者正在执行中的stage
    private Set<String> dispatchedStages;
    private Map<String, CompletableFuture<?>> stageNameFutures;

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

    public void updateStageRuntimeEnv(String stageName, StageRuntimeEnv<?> stageRuntimeEnv) {
        this.stageRuntimeEnvs.put(stageName, stageRuntimeEnv);
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
