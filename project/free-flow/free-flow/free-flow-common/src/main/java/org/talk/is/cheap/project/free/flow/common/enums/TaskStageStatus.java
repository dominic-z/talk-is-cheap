package org.talk.is.cheap.project.free.flow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum TaskStageStatus {

    // 流转PENDING->RUNNING->SUCCEEDED/FAILING/FAILED/TERMINATING/TERMINATED/SKIPPED
    PENDING(0, "PENDING"),
    FAILED_TO_START(1, "FAILED_TO_START"),
    RUNNING(2, "RUNNING"),
    SUCCEEDED(3, "SUCCEEDED"),
    FAILING(4, "FAILED"),
    FAILED(5, "FAILED"),
    TERMINATING(6, "TERMINATING"),
    TERMINATED(7, "TERMINATED"),
    SKIPPED(8, "SKIPPED"),
    RESCHEDULING(9, "RESCHEDULING"),
    RESCHEDULED(10, "RESCHEDULED"),
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
