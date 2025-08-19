package org.talk.is.cheap.project.free.flow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum NodeStatus {

    // 流转RUNNABLE->QUIT_RUNNABLE->TERMINATING->TERMINATED
    RUNNABLE(0, "RUNNABLE"),
    TERMINATED(1, "TERMINATED"),
    TERMINATING(2, "TERMINATING"),
    RUNNABLE_TERMINATING(3, "RUNNABLE_TERMINATING"); // worker节点从runnable状态退出，进入terminating之前的中间状态，因为这个变换不是原子的，所以新增一个中间态

    @Getter
    private final Integer status;
    @Getter
    private final String desc;


    private final static Map<Integer, NodeStatus> STATUS_MAP = new HashMap<>();

    static {
        for (NodeStatus nodeStatus : NodeStatus.values()) {
            STATUS_MAP.put(nodeStatus.getStatus(), nodeStatus);
        }
    }

    public static NodeStatus getByStatus(Integer status) {
        return STATUS_MAP.get(status);
    }
}
