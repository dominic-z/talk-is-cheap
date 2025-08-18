package org.talk.is.cheap.project.free.flow.common.message.impl.vo;

import lombok.Builder;
import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TaskDefinitionVO {
    private String name;
    private Integer version;
    private Integer maxRetryCount;
    private Integer timeoutInSecond;

    // stage定义
    private Map<String, StageDefinitionVO> stageDefinitionVOMap;

    // stage的链接情况
    private Map<String, List<String>> pointOutGraph;

    // 图的根节点
    private List<String> roots;
}
