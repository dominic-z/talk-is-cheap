package org.talk.is.cheap.project.free.flow.common.task.definition.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TaskDefinitionBO {

    private String name;
    private Integer version;
    private Integer maxRetryCount;
    private Integer timeoutInSecond;

    // stage定义
    @Builder.Default
    private final Map<String, StageDefinitionBO> stageDefinitionBOMap = new HashMap<>();

    // stage的链接情况
    @Builder.Default
    private final Map<String, Set<String>> pointOutGraph = new HashMap<>();

    // 图的根节点
    @Builder.Default
    private final Set<String> roots = new HashSet<>();
}
