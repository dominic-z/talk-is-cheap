package org.talk.is.cheap.project.what.to.eat.util;

import org.talk.is.cheap.project.what.to.eat.constants.ErrorCode;
import org.talk.is.cheap.project.what.to.eat.exceptions.VerificationException;

public class VerifyUtil {
    private VerifyUtil() {

    }

    public static void notNull(Object o, int errorCode, String errMsg) throws VerificationException {
        if (o == null) {
            throw new VerificationException(errorCode, errMsg);
        }
    }

    public static void notNull(Object o, String errMsg) throws VerificationException {
        notNull(o, ErrorCode.ILLEGAL_PARAMETER_ERROR, errMsg);
    }


    /**
     * 使用了自限定泛型，T必须继承了Comparable<T>，比如Long就是继承了Comparable<Long>
     *
     * @param a
     * @param b
     * @param errMsg
     * @param <T>
     */
    public static <T extends Comparable<T>> void gt(T a, T b, int errorCode, String errMsg) throws VerificationException {
        if (a.compareTo(b) <= 0) {
            throw new VerificationException(errorCode, errMsg);
        }
    }

    public static <T extends Comparable<T>> void gt(T a, T b, String errMsg) throws VerificationException {
        gt(a, b, ErrorCode.ILLEGAL_PARAMETER_ERROR, errMsg);
    }
}
