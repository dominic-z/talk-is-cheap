package org.talk.is.cheap.project.free.flow.common.task.definition.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TaskDefinitionBO {

    private String name;
    private Integer version;
    // 上下文共享对象的编解码器的类型
    private Class<?> sharedContextCodecClass;
    // 上下文共享对象的真实的类型，是各个stage共享的一个数据
    private Class<?> sharedContextClass;
    private Integer maxRetryCount;
    private Integer timeoutInSecond;

    // stage定义
    @Builder.Default
    private Map<String, StageDefinitionBO> stageDefinitionMap = new HashMap<>();

    // stage的链接情况
    @Builder.Default
    private Map<String, Set<String>> pointOutGraph = new HashMap<>();

    // 图的根节点
    @Builder.Default
    private Set<String> roots = new HashSet<>();


    /**
     * 用于任务执行时通过反射时调用方法
     */
    // task任务对象（即被@Task注解标记的对象）的真实的类信息
    private Class<?> taskClass;

}
