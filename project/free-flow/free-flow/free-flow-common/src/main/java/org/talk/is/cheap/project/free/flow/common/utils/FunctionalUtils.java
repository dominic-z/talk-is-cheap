package org.talk.is.cheap.project.free.flow.common.utils;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class FunctionalUtils {

    public static <R> void tryExecute(Supplier<R> supplier, Predicate<R> predicate, int maxRetryCount) {

        for (int i = 0; i < maxRetryCount; i++) {

            R r = supplier.get();
            if (predicate.test(r)) {
                break;
            }

            VerifyUtil.requireTrue(i + 1 < maxRetryCount, "Exceeded the maximum retry count");
        }
    }
}
