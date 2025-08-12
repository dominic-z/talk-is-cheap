package org.talk.is.cheap.project.free.flow.starter.repository.domain.enums;

import lombok.Getter;

public enum NodeStatus {

    // 流转RUNNABLE->QUIT_RUNNABLE->TERMINATING->TERMINATED
    RUNNABLE(0, "RUNNABLE"),
    TERMINATED(1, "TERMINATED"),
    TERMINATING(2,"TERMINATING"),
    QUIT_RUNNABLE(3, "QUIT_RUNNABLE"); // worker节点从runnable状态退出，进入terminating之前的中间状态

    @Getter
    private final Integer status;
    @Getter
    private final String desc;

    NodeStatus(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static NodeStatus getByStatus(Integer status) {
        for (NodeStatus e : NodeStatus.values()) {
            if (e.status.equals(status)) {
                return e;
            }
        }
        return null;
    }
}
