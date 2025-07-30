package org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.enums;

import lombok.Getter;

public enum NodeAction {

    UP(0, "UP"), DOWN(1, "DOWN");

    @Getter
    private final Integer action;
    @Getter
    private final String desc;

    NodeAction(Integer action, String desc) {
        this.action = action;
        this.desc = desc;
    }

    public static NodeAction getByType(Integer type) {
        for (NodeAction e : NodeAction.values()) {
            if (e.action.equals(type)) {
                return e;
            }
        }
        return null;
    }
}
