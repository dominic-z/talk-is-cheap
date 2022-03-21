package com.example.components;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @date 2020/8/18 4:57 下午
 */
@Component("methodInjectApple")
public abstract class MethodInjectApple {
    @Lookup("protoApple")
    public abstract ProtoApple getApple();
}
