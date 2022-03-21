package com.example.components;

import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @date 2020/8/18 4:13 下午
 */
@Component("argApple")
@Data
public class ConstructArgApple {
    private String name;
    private Apple apple;
    private String plainStr;

    public ConstructArgApple(@Value("${argApple.name}") String name, @Qualifier("apple") Apple apple, @Value("plainStr") String plainStr) {
        this.name = name;
        this.apple = apple;
        this.plainStr = plainStr;
    }
}
