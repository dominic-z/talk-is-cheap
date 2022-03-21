package com.example.components;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author dominiczhu
 * @date 2020/8/12 10:20 上午
 */
@Component("apple")
@Data
public class Apple {
    @Value("${apple.name}")
    private String name;

    @PostConstruct
    public void postConstructed() {
        System.out.println("apple name:" + name);
    }
}
