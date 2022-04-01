package org.talk.is.cheap.spring.cloud.sleuth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.spring.cloud.sleuth.client.BackendClient;

/**
 * @author dominiczhu
 * @version 1.0
 * @title SleuthController
 * @date 2022/3/29 1:33 下午
 */
@RestController
@RequestMapping("/frontend")
@Slf4j
public class FrontendController {

    private static final String LOG_MARKER = "[frontend]";

    @Autowired
    private BackendClient backendClient;

    @GetMapping("/hello")
    public String hello() {
        log.info("Hello world!");
        return "Hello World!";
    }

    @GetMapping("/echo")
    public String echo(@RequestParam("value") String value) {
        log.info("{} echo {}", LOG_MARKER, value);
        final String echo = backendClient.echo(value);
        log.info("{} return {}", LOG_MARKER, echo);
        return echo;
    }
}
