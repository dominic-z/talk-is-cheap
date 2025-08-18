package org.talk.is.cheap.project.free.flow.common.utils;

import com.google.common.base.VerifyException;
import org.apache.commons.lang3.StringUtils;

public class VerifyUtil {

    private VerifyUtil() {
    }

    public static void isTrue(boolean bool, String errorMsg) {
        if (!bool) {
            throw new VerifyException(errorMsg);
        }
    }

    public static void isNotBlank(String s, String errorMsg) {
        isTrue(StringUtils.isNotBlank(s), errorMsg);
    }

    public static void isNotNull(Object o, String errorMsg) {
        isTrue(o != null, errorMsg);
    }




}
