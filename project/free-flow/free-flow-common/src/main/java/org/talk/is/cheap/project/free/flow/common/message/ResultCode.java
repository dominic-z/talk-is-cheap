package org.talk.is.cheap.project.free.flow.common.message;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


@AllArgsConstructor
public enum ResultCode {
    SUCCESS(0, ""),
    FAIL(100, "fail"),
    ILLEGAL_ARGUMENT(101, "illegal_argument"),
    VERIFY_FAIL(102, "verify fail"),
    ;

    @Getter
    private final Integer code;
    @Getter
    private final String msg;

    private static final Map<Integer, ResultCode> CODE_MAP = new HashMap<>();

    static {
        for (ResultCode value : ResultCode.values()) {
            CODE_MAP.put(value.getCode(), value);
        }
    }

    public static ResultCode getByCodeVal(int code) {
        return CODE_MAP.get(code);
    }


}

