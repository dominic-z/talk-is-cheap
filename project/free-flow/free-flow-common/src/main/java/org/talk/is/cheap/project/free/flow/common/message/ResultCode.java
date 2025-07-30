package org.talk.is.cheap.project.free.flow.common.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;


@AllArgsConstructor
public enum ResultCode {
    SUCCESS(0, ""),
    FAIL(100,"fail")
    ;

    @Getter
    private final Integer code;
    @Getter
    private final String msg;


}

