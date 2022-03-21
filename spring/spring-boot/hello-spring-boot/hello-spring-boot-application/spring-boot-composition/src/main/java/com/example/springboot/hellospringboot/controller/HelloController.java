package com.example.springboot.hellospringboot.controller;

import com.example.springboot.hellospringboot.domain.messages.HelloRequest;
import com.example.springboot.hellospringboot.domain.messages.HelloResponse;
import com.example.springboot.hellospringboot.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dominiczhu
 * @version 1.0
 * @title HelloController
 * @date 2021/9/14 下午8:20
 */
@RestController()
@RequestMapping(path = "/web/hello")
@Slf4j
public class HelloController {

    @Autowired
    private HelloService helloService;

    @ResponseBody
    @RequestMapping(path = "/getHelloWorld", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public HelloResponse getHelloWorld(@RequestParam("content") String content) {
        HelloResponse resp = new HelloResponse();
        log.info("content is {}", content);
        helloService.sayHello();
        resp.setContent(content);
        return resp;
    }

    @ResponseBody
    @RequestMapping(path = "/postHelloWorld", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public HelloResponse postHelloWorld(@RequestBody HelloRequest helloReq) {
        HelloResponse resp = new HelloResponse();
        log.info("hello Req is {}", helloReq.toString());
        helloService.sayHello();
        resp.setContent(helloReq.getContent());
        return resp;
    }

    @ResponseBody
    @RequestMapping(path = "/putHelloWorld", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public HelloResponse putHelloWorld(@RequestBody HelloRequest helloReq) {
        HelloResponse resp = new HelloResponse();
        log.info("hello Req is {}", helloReq.toString());
        helloService.sayHello();
        resp.setContent(helloReq.getContent());
        return resp;
    }

}
