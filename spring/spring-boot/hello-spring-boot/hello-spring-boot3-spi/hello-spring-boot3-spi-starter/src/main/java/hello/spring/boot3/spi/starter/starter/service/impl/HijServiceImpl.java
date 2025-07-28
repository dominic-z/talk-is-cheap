package hello.spring.boot3.spi.starter.starter.service.impl;

import hello.spring.boot3.spi.starter.starter.service.HijService;

public class HijServiceImpl implements HijService {
    @Override
    public String hij() {
        return "hij from starter";
    }
}
