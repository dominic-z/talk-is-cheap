package org.talk.is.cheap.project.what.to.eat.util;

import org.talk.is.cheap.project.what.to.eat.domain.message.GenericResponse;

public class ResultUtil {

    private ResultUtil() {
    }


    public static <T extends GenericResponse<?>> T success(T resp) {
        resp.setCode(0);
        return resp;
    }

    public static <T extends GenericResponse<?>> T fail(T resp, int errCode, String errMsg) {
        resp.setCode(errCode);
        resp.setMessage(errMsg);
        return resp;
    }
}
