package org.talk.is.cheap.project.free.common.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;


@AllArgsConstructor
public enum ResultCode {
    SUCCESS(0, ""),
    FAIL(100,"fail")
    ;

    @Getter
    private final int code;
    @Getter
    private final String msg;


}

