package org.talk.is.cheap.spring.cloud.ribbon.eureka.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dominiczhu
 * @version 1.0
 * @title FrontendController
 * @date 2022/3/31 1:13 下午
 */
@RestController
@RequestMapping("/backend")
@Slf4j
public class BackendController {

    private static final String LOG_MARKER = "[BackendController]";

    @GetMapping("/echo")
    public String echo(@RequestParam("value") String value) {
        log.info("{} 1000 echo value: {}", LOG_MARKER, value);
        return LOG_MARKER + value+"server1000";
    }

}
