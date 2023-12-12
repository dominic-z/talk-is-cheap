package org.talk.is.cheap.project.what.to.eat.util;

import org.talk.is.cheap.project.what.to.eat.constants.ErrorCode;
import org.talk.is.cheap.project.what.to.eat.exceptions.VerificationException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

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

    public static void isTrue(boolean b, int errorCode, String errMsg) throws VerificationException {
        if (!b) {
            throw new VerificationException(errorCode, errMsg);
        }
    }


    public static void isTrue(boolean b, String errMsg) throws VerificationException {
        if (!b) {
            throw new VerificationException(ErrorCode.ILLEGAL_PARAMETER_ERROR, errMsg);
        }
    }


    public static void isFalse(boolean b, int errorCode, String errMsg) throws VerificationException {
        if (b) {
            throw new VerificationException(errorCode, errMsg);
        }
    }


    public static void isFalse(boolean b, String errMsg) throws VerificationException {
        if (b) {
            throw new VerificationException(ErrorCode.ILLEGAL_PARAMETER_ERROR, errMsg);
        }
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

    public static <T extends Comparable<T>> void gte(T a, T b, int errorCode, String errMsg) throws VerificationException {
        if (a.compareTo(b) < 0) {
            throw new VerificationException(errorCode, errMsg);
        }
    }


    public static <T extends Comparable<T>> void gte(T a, T b, String errMsg) throws VerificationException {
        gte(a, b, ErrorCode.ILLEGAL_PARAMETER_ERROR, errMsg);
    }

    public static <T extends Comparable<T>> void between(T o, T lowerBoundary, T upperBoundary, int errorCode,
                                                         String errMsg) throws VerificationException {
        if (o.compareTo(lowerBoundary) <= 0 || o.compareTo(upperBoundary) >= 0) {
            throw new VerificationException(errorCode, errMsg);
        }
    }


    public static <T extends Comparable<T>> void between(T o, T lowerBoundary, T upperBoundary,
                                                         String errMsg) throws VerificationException {
        between(o, lowerBoundary, upperBoundary, ErrorCode.ILLEGAL_PARAMETER_ERROR, errMsg);
    }

    public static <T extends Comparable<T>> void eq(T a, T b, int errorCode, String errMsg) throws VerificationException {
        if (a.compareTo(b) != 0) {
            throw new VerificationException(errorCode, errMsg);
        }
    }


    public static <T extends Comparable<T>> void eq(T a, T b, String errMsg) throws VerificationException {
        eq(a, b, ErrorCode.ILLEGAL_PARAMETER_ERROR, errMsg);
    }


    public static <T extends Collection<?>> void notEmpty(T a, int errorCode, String errMsg) throws VerificationException {
        if (a.isEmpty()) {
            throw new VerificationException(errorCode, errMsg);
        }
    }

    public static <T extends Collection<?>> void notEmpty(T a, String errMsg) throws VerificationException {
        notEmpty(a, ErrorCode.ILLEGAL_PARAMETER_ERROR, errMsg);
    }

    public static <T extends Collection<?>> void notNullOrEmpty(T a, int errorCode, String errMsg) throws VerificationException {
        if (a == null || a.isEmpty()) {
            throw new VerificationException(errorCode, errMsg);
        }
    }

    public static <T extends Collection<?>> void notNullOrEmpty(T a, String errMsg) throws VerificationException {
        notEmpty(a, ErrorCode.ILLEGAL_PARAMETER_ERROR, errMsg);
    }
}