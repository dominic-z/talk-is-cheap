package hello.spring.boot3.spi.starter.service.impl;


import hello.spring.boot3.spi.starter.service.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloServiceImpl implements HelloService {

    public String sayHello(){
        log.info("hello from starter");
        return "hello from starter";
    }
}
