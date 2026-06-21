package hello.spring.boot3.spi.starter.starter.service.impl;

import hello.spring.boot3.spi.starter.starter.service.HiService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HiServiceImpl implements HiService {
    @Override
    public String hi() {
        log.info("hi from starter");
        return "hi from starter";
    }
}
