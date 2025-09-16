package org.talk.is.cheap.project.free.flow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum TaskStageStatus {

    // 流转PENDING->RUNNING->SUCCEEDED/FAILING/FAILED/TERMINATING/TERMINATED/SKIPPED
    PENDING(0, "PENDING"),
    RUNNING(1, "RUNNING"),
    SUCCEEDED(2, "SUCCEEDED"),
    FAILING(3, "FAILED"),
    FAILED(4, "FAILED"),
    TERMINATING(5, "TERMINATING"),
    TERMINATED(6, "TERMINATED"),
    SKIPPED(7, "SKIPPED"),
    ;

    @Getter
    private final Integer status;
    @Getter
    private final String desc;

    private final static Map<Integer, TaskStageStatus> STATUS_MAP = new HashMap<>();

    static {
        for (TaskStageStatus nodeStatus : TaskStageStatus.values()) {
            STATUS_MAP.put(nodeStatus.getStatus(), nodeStatus);
        }
    }

    public static TaskStageStatus getByStatus(Integer status) {
        return STATUS_MAP.get(status);
    }
}
