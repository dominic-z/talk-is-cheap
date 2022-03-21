package com.example.controllers;


import com.example.components.Banana1;
import com.example.components.Banana2;
import com.example.messages.MyRequest;
import com.example.config.SpringContextConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author dominiczhu
 * @date 2020/8/12 10:20 上午
 */

@RestController
public class HelloController {
    @Resource(name = "singleBanana1")
    Banana1 banana1;

    @Autowired
    @Qualifier("singleBanana2")
    Banana2 banana2;

    @Autowired
    private SpringContextConfig springContextConfig;

    @RequestMapping(value = "hello", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    public String hello(@RequestBody MyRequest req) {

        Object apple = springContextConfig.getApplicationContext().getBean("apple");
        System.out.println(apple);
        System.out.println(banana1);
        System.out.println(banana2);
        return "hello world";
    }

}
