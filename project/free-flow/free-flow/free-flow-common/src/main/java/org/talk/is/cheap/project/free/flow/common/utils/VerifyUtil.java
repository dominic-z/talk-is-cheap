package org.talk.is.cheap.project.free.flow.common.utils;

import com.google.common.base.VerifyException;
import org.apache.commons.lang3.StringUtils;

public class VerifyUtil {

    private VerifyUtil() {
    }

    public static void shallBeTrue(boolean bool, String errorMsg) {
        if (!bool) {
            throw new VerifyException(errorMsg);
        }
    }

    public static void shallBeFalse(boolean bool, String errorMsg) {
        if (bool) {
            throw new VerifyException(errorMsg);
        }
    }

    public static void shallNotBeBlank(String s, String errorMsg) {
        shallBeTrue(StringUtils.isNotBlank(s), errorMsg);
    }

    public static void shallNotBeNull(Object o, String errorMsg) {
        shallBeTrue(o != null, errorMsg);
    }




}
