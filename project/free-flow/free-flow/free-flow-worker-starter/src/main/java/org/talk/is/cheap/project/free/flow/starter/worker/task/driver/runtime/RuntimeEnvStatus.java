package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime;

import lombok.Getter;
import org.talk.is.cheap.project.free.flow.common.enums.TaskStageStatus;

import java.util.HashMap;
import java.util.Map;

public enum RuntimeEnvStatus {
    RUNNING(1, "RUNNING"),
    RESCHEDULING(2, "RESCHEDULING"),
    RESCHEDULED(3, "RESCHEDULED"),
    TERMINATING(4, "TERMINATING"),
    FAILED(5, "FAILED"),
    SUCCEED(6, "SUCCEED"),
    TIME_OUT(7, "TIME_OUT");

    RuntimeEnvStatus(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    @Getter
    private final Integer status;
    @Getter
    private final String desc;

    private final static Map<Integer, RuntimeEnvStatus> STATUS_MAP = new HashMap<>();

    static {
        for (RuntimeEnvStatus envStatus : RuntimeEnvStatus.values()) {
            if (STATUS_MAP.containsKey(envStatus.getStatus())) {
                throw new RuntimeException("枚举值重复");
            }
            STATUS_MAP.put(envStatus.getStatus(), envStatus);
        }
    }

    public static RuntimeEnvStatus getByStatus(Integer status) {
        return STATUS_MAP.get(status);
    }
}
