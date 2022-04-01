package org.talk.is.cheap.spring.cloud.sleuth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dominiczhu
 * @version 1.0
 * @title BackendControllerImpl
 * @date 2022/3/29 1:56 下午
 */
@Slf4j
@RestController
@RequestMapping("/backend")
public class BackendController {

    private static final String LOG_MARKER = "[BackendController]";

    @GetMapping("/echo")
    public String echo(@RequestParam("value") String value) {
        log.info("{} echo {}", LOG_MARKER, value);
        return LOG_MARKER + value;
    }
}
