package org.talk.is.cheap.project.free.flow.common.utils;

import com.google.common.base.VerifyException;
import org.apache.commons.lang3.StringUtils;

public class VerifyUtil {

    private VerifyUtil() {
    }

    public static void requireTrue(boolean bool, String errorMsg) {
        if (!bool) {
            throw new VerifyException(errorMsg);
        }
    }

    public static void requireAllTrue(String errorMsg, boolean... booleans) {
        for (boolean aBoolean : booleans) {
            requireTrue(aBoolean, errorMsg);
        }
    }

    public static void requireFalse(boolean bool, String errorMsg) {
        if (bool) {
            throw new VerifyException(errorMsg);
        }
    }

    public static void requireAllFalse(String errorMsg, boolean... booleans) {
        for (boolean aBoolean : booleans) {
            requireFalse(aBoolean, errorMsg);
        }
    }

    public static void requireNotBlank(String s, String errorMsg) {
        requireTrue(StringUtils.isNotBlank(s), errorMsg);
    }

    public static void requireNotNull(Object o, String errorMsg) {
        requireTrue(o != null, errorMsg);
    }

    public static void requireAllNotNull(String errorMsg, Object... objects) {
        for (Object object : objects) {
            requireNotNull(object, errorMsg);
        }
    }


}
