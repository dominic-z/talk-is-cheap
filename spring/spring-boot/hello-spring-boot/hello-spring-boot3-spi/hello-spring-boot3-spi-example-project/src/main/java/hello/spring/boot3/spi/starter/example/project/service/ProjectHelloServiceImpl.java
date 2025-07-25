package hello.spring.boot3.spi.starter.example.project.service;

import hello.spring.boot3.spi.starter.service.HelloService;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ProjectHelloServiceImpl implements HelloService {
    @Override
    public String sayHello() {
        log.info("hello from project");
        return "hello from project";
    }
}
