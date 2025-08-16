package org.talk.is.cheap.project.free.flow.common.message.impl.vo;

import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.message.impl.QueryTaskDefinitionResp;

import java.util.List;
import java.util.Map;

@Data
public class StageDefinitionVO{
    private Long id;
    private Long taskId;
    private String stageName;
    private Integer version;
    private Integer stageType;
    private Boolean isStartingStage;
    private Integer timeout;
    private Integer maxRetryCount;

}