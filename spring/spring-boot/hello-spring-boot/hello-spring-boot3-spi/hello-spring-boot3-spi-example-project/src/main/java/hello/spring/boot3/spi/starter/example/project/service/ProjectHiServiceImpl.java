package hello.spring.boot3.spi.starter.example.project.service;

import hello.spring.boot3.spi.starter.service.HiService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectHiServiceImpl implements HiService {
    @Override
    public String hi() {
        log.info("hi from project");
        return "hi from project";
    }
}
