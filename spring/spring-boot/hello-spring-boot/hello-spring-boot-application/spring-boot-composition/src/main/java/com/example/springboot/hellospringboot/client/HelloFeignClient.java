package com.example.springboot.hellospringboot.client;

import com.example.springboot.hellospringboot.domain.messages.HelloRequest;
import com.example.springboot.hellospringboot.domain.messages.HelloResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author dominiczhu
 * @version 1.0
 * @title TestFeignClient
 * @date 2022/2/15 1:26 下午
 */
@FeignClient(value = "spring-boot-composition")
public interface HelloFeignClient {


    @RequestMapping(path = "/web/hello/postHelloWorld", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    HelloResponse postHelloWorld(@RequestBody HelloRequest helloReq);

    @RequestMapping(path = "/web/hello/getHelloWorld", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    HelloResponse getHelloWorld(@RequestParam("content") String content);

}
