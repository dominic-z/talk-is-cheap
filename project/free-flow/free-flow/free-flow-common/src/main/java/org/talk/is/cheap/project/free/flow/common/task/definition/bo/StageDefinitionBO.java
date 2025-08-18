package org.talk.is.cheap.project.free.flow.common.task.definition.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.task.definition.codec.InputCodec;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class StageDefinitionBO {

    private String name;
    private Integer version;
    private Class<? extends InputCodec<?>> inputCodecClass;
    private Boolean isStartingStage;
    private List<String> toStageNames; 
    private List<String> fromStageNames;

    // 用于反射
    private String methodName;
    private Parameter[] parameters;
}
