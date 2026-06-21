package org.talk.is.cheap.project.free.flow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum NodeStatus {

    // 流转RUNNABLE->QUIT_RUNNABLE->TERMINATING->TERMINATED
    INITIALIZING(0,"初始化中"),
    RUNNABLE(1, "可以运行任务"),
    TERMINATING(2, "终止中"),
    SAFE_TO_TERMINATE(3, "可以安全退出"),
    TERMINATED(4, "已经终止");
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
