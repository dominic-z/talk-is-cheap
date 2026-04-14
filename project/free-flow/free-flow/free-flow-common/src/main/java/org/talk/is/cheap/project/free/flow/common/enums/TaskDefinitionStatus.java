package org.talk.is.cheap.project.free.flow.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum TaskDefinitionStatus {


    HAS_AVAILABLE_WORKER(0,"有worker可以执行"),
    HAS_NO_AVAILABLE_WORKER(1,"无worker可以执行")
    ;
    @Getter
    private final int type;
    @Getter
    private final String desc;

    private final static Map<Integer, TaskDefinitionStatus> TYPE_MAP = new HashMap<>();

    static {
        for (TaskDefinitionStatus nodeType : TaskDefinitionStatus.values()) {
            TYPE_MAP.put(nodeType.getType(), nodeType);
        }
    }

    public static TaskDefinitionStatus getByStatus(Integer status) {
        return TYPE_MAP.get(status);
    }

}
