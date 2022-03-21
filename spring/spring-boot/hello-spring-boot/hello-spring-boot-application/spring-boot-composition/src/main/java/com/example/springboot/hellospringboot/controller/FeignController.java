package com.example.springboot.hellospringboot.controller;

import com.example.springboot.hellospringboot.client.HelloFeignClient;
import com.example.springboot.hellospringboot.domain.messages.HelloRequest;
import com.example.springboot.hellospringboot.domain.messages.HelloResponse;
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
 * @title FeignController
 * @date 2022/2/15 1:36 下午
 */
@RestController()
@RequestMapping(path = "/web/feign")
@Slf4j
public class FeignController {

    @Autowired
    private HelloFeignClient helloFeignClient;

    @ResponseBody
    @RequestMapping(path = "/getHelloWorld", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public HelloResponse getHelloWorld(@RequestParam("content") String content) {
        return helloFeignClient.getHelloWorld(content);
    }

    @ResponseBody
    @RequestMapping(path = "/postHelloWorld", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public HelloResponse postHelloWorld(@RequestBody HelloRequest helloReq) {
        return helloFeignClient.postHelloWorld(helloReq);
    }
}
