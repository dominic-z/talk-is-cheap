package org.talk.is.cheap.project.free.flow.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum StageType {


    RUNNABLE(0,"runnable"),
    CONTROL(1,"control")
    ;
    @Getter
    private final int type;
    @Getter
    private final String desc;

    private final static Map<Integer, StageType> TYPE_MAP = new HashMap<>();

    static {
        for (StageType nodeType : StageType.values()) {
            TYPE_MAP.put(nodeType.getType(), nodeType);
        }
    }

    public static StageType getByStatus(Integer status) {
        return TYPE_MAP.get(status);
    }

}
