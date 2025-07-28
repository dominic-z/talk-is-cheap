package org.talk.is.cheap.project.free.common.enums;

import org.apache.commons.lang3.StringUtils;


/**
 * 运行在虚拟机中还是运行在主机中，用于决定注册是注册ip还是容器名称，以便后续的网路链接
 */
public enum EnvType {
    // 主机
    HOST,
    //        容器
    CONTAINER;

    public static EnvType getByName(String s) {
        for (EnvType e : EnvType.values()) {
            if (StringUtils.equals(e.name(), StringUtils.toRootUpperCase(s))) {
                return e;
            }
        }
        return null;
    }
}
