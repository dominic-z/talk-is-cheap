package org.talk.is.cheap.project.free.flow.common.message.impl.dto;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class TaskDefinitionDTO {
    private Integer id;
    private String name;
    private Integer version;
    private Integer maxRetryCount;
    private Integer timeout;

    private String sharedContextCodecFullyQualifiedClassName;
    private String sharedContextFullyQualifiedClassName;
    // stage定义
    private Map<String, StageDefinitionDTO> stageDefinitionMap;

    // stage的链接情况
    private Map<String, Set<String>> pointOutGraph;

    // 图的根节点
    private Set<String> roots;
}
