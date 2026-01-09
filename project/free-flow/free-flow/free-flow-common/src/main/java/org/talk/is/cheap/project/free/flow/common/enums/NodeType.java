package org.talk.is.cheap.project.free.flow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum NodeType {

    SCHEDULER_LEADER(0, "scheduler-leader"),
    SCHEDULER(1, "scheduler"),
    WORKER(2, "worker");

    @Getter
    private final Integer type;
    @Getter
    private final String desc;

    private final static Map<Integer, NodeType> TYPE_MAP = new HashMap<>();

    static {
        for (NodeType nodeType : NodeType.values()) {
            TYPE_MAP.put(nodeType.getType(), nodeType);
        }
    }


    public static NodeType getByType(Integer type) {
        return TYPE_MAP.get(type);
    }
}
