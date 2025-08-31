package org.talk.is.cheap.project.free.flow.common.task.definition.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.task.definition.codec.InputCodec;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class StageDefinitionBO {

    private String name;
    private Integer version;
    private Class<? extends InputCodec<?>> inputCodecClass;
    private Boolean isStartingStage;
    @Builder.Default
    private Set<String> toStageNames = new HashSet<>();
    @Builder.Default
    private Set<String> fromStageNames = new HashSet<>();

    private int maxRetryCount;
    private int timeout;

    // 用于反射
    private String methodName;
    private Parameter[] parameters;
}
