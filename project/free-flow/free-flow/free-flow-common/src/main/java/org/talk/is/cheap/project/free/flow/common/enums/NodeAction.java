package org.talk.is.cheap.project.free.flow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum NodeAction {

    // 流转RUNNABLE->QUIT_RUNNABLE->TERMINATING->TERMINATED
    INITIALIZING(0,"初始化中"),
    RUNNABLE(0, "可以运行任务"),
    TERMINATING(1, "终止中"),
    TERMINATED(2, "已经终止"),
    RUNNABLE_TO_TERMINATING(3, "RUNNABLE_TERMINATING"); // worker节点从runnable状态退出，进入terminating之前的中间状态，因为这个变换不是原子的，所以新增一个中间态

    @Getter
    private final Integer status;
    @Getter
    private final String desc;


    private final static Map<Integer, NodeAction> STATUS_MAP = new HashMap<>();

    static {
        for (NodeAction nodeStatus : NodeAction.values()) {
            STATUS_MAP.put(nodeStatus.getStatus(), nodeStatus);
        }
    }

    public static NodeAction getByStatus(Integer status) {
        return STATUS_MAP.get(status);
    }
}
