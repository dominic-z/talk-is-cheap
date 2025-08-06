package org.talk.is.cheap.project.free.flow.starter.worker.task.definition.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.starter.worker.task.codec.InputCodec;

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

    // 用于反射
    private String methodName;
    private Parameter[] parameters;
}
