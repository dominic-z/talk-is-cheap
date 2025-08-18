package org.talk.is.cheap.project.free.flow.common.message.impl.vo;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class TaskDefinitionVO {
    private String name;
    private Integer version;
    private Integer maxRetryCount;
    private Integer timeout;

    // stage定义
    private Map<String, StageDefinitionVO> stageDefinitionVOMap;

    // stage的链接情况
    private Map<String, Set<String>> pointOutGraph;

    // 图的根节点
    private Set<String> roots;
}
