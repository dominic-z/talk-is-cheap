package hello.spring.boot3.spi.starter.starter.service.impl;

import hello.spring.boot3.spi.starter.starter.service.KonnichiwaService;

public class KonnichiwaServiceImpl implements KonnichiwaService {
    @Override
    public String konnichiwa() {
        return "Konnichiwa from starter";
    }
}
