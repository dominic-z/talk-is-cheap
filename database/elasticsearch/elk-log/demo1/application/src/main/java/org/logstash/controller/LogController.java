package org.logstash.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
public class LogController {

    @GetMapping("/index")
    public String index() {
        String uuid = UUID.randomUUID().toString();
        log.info("TestController info " + uuid);
        return "hello elk " + uuid;
    }
}
