package org.talk.is.cheap.project.free.flow.common.task.definition.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.enums.StageType;
import org.talk.is.cheap.project.free.flow.common.task.codec.InputCodec;

import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class StageDefinitionBO {

    private String name;
    private Integer version;
    private StageType stageType;
    private Class<? extends InputCodec<?>> inputCodecClass;
    private Class<?> inputClass;
    private Boolean isStartingStage;
    @Builder.Default
    private Set<String> toStageNames = new HashSet<>();
    @Builder.Default
    private Set<String> fromStageNames = new HashSet<>();

    private int maxRetryCount;
    private int timeout;

    // 用于任务执行时通过反射时调用方法
    private String methodName;
    private Parameter[] parameters;
}
