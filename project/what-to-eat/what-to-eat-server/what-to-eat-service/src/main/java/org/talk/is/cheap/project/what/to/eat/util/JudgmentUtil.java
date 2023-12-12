package org.talk.is.cheap.project.what.to.eat.util;

import java.util.Collection;

public class JudgmentUtil {
    private JudgmentUtil() {
    }

    public static <T extends Collection<?>> boolean isNullOrIsEmpty(T t) {
        return t == null || t.isEmpty();
    }

    public static <T extends Collection<?>> boolean notNullAndNotEmpty(T t) {
        return !isNullOrIsEmpty(t);
    }
}
