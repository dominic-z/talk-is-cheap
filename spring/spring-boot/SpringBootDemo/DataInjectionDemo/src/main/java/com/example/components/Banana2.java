package com.example.components;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @date 2020/8/12 11:16 上午
 */
@Component(value = "singleBanana2")
@Data
public class Banana2 {
    @Value("${banana.name}")
    private String name;
}
