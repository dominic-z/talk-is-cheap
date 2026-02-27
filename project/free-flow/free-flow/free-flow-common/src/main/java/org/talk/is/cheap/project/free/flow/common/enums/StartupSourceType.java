package org.talk.is.cheap.project.free.flow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


@AllArgsConstructor
public enum StartupSourceType {

    EXTERNAL(0,"调用来源来自于外界，例如外界希望启动某个task、stage"),
    OTHER_STAGE_EXECUTION(1,"调用来源来自于其他stage的execution"),
    OTHER_TASK_EXECUTION(2,"调用来源来自于其他task的execution，例如scheduleTask"),
    TASK_RESUME(3,"Task的执行来自于恢复其他task的执行"),

    ;

    @Getter
    private final int value;

    @Getter
    private final String desc;


    private final static Map<Integer, StartupSourceType> VALUE_MAP = new HashMap<>();

    static {
        for (StartupSourceType e : StartupSourceType.values()) {
            VALUE_MAP.put(e.getValue(), e);
        }
    }

    public static StartupSourceType getByValue(Integer status) {
        return VALUE_MAP.get(status);
    }
}
