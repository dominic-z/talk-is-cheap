package org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.enums;

import lombok.Getter;

public enum NodeType {

    SCHEDULER(0, "scheduler"), WORKER(1, "worker");

    @Getter
    private final Integer type;
    @Getter
    private final String desc;

    NodeType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static NodeType getByType(Integer type) {
        for (NodeType e : NodeType.values()) {
            if (e.type.equals(type)) {
                return e;
            }
        }
        return null;
    }
}
