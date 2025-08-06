package org.talk.is.cheap.project.free.flow.starter.worker.task.definition.bo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, StageDefinitionBO> stageDefinitionBOMap = new HashMap<>();
    // stage的链接情况

    @Builder.Default
    private Map<String, List<String>> graph = new HashMap<>();

    // 图的根节点
    @Builder.Default
    private List<String> roots = new ArrayList<>();
}
