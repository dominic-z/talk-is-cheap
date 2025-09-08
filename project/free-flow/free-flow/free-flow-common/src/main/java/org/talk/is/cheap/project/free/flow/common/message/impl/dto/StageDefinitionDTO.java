package org.talk.is.cheap.project.free.flow.common.message.impl.dto;

import lombok.Data;

@Data
public class StageDefinitionDTO {
    private Long id;
    private Long taskId;
    private String stageName;
    private Integer version;
    private String inputCodecFullyQualifiedClassName;
    private String inputFullyQualifiedClassName;
    private Integer stageType;
    private Boolean isStartingStage;
    private Integer timeout;
    private Integer maxRetryCount;

}