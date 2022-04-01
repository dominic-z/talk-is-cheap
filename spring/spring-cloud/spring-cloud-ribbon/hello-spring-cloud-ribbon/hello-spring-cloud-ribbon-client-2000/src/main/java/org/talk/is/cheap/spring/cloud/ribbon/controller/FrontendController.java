package org.talk.is.cheap.spring.cloud.ribbon.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.spring.cloud.ribbon.client.FeignClientApi;

/**
 * @author dominiczhu
 * @version 1.0
 * @title FrontendController
 * @date 2022/3/31 1:13 下午
 */
@RestController
@RequestMapping("/frontend")
@Slf4j
public class FrontendController {

    private static final String LOG_MARKER = "[FrontController]";

    @Autowired
    private FeignClientApi feignClientApi;

    @GetMapping("/echo")
    public String echo(@RequestParam("value") String value) {
        log.info("{} frontend value:{}", LOG_MARKER, value);
        return feignClientApi.echo(value);
    }

}
