package org.talk.is.cheap.project.free.flow.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum TaskExecutionErrorCode {
    TASK_TIME_OUT(0, "任务超时"),
    STAGE_TIME_OUT(1, "任务超时"),
    BIZ_ERROR(2, "业务异常"),
    ;

    @Getter
    private final Integer code;
    @Getter
    private final String msg;

    private static final Map<Integer, ResultCode> CODE_MAP = new HashMap<>();

    static {
        for (ResultCode value : ResultCode.values()) {
            if (CODE_MAP.containsKey(value.getCode())) {
                throw new RuntimeException("code重复");
            }
            CODE_MAP.put(value.getCode(), value);
        }
    }

    public static ResultCode getByCodeVal(int code) {
        return CODE_MAP.get(code);
    }

}
