package org.talk.is.cheap.project.free.flow.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum TaskType {


    TASK(0,"task"),
    SCHEDULE_TASK(1,"SCHEDULE_TASK")
    ;
    @Getter
    private final int type;
    @Getter
    private final String desc;

    private final static Map<Integer, TaskType> TYPE_MAP = new HashMap<>();

    static {
        for (TaskType nodeType : TaskType.values()) {
            TYPE_MAP.put(nodeType.getType(), nodeType);
        }
    }

    public static TaskType getByStatus(Integer status) {
        return TYPE_MAP.get(status);
    }

}
