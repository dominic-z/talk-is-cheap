package org.talk.is.cheap.orm.mybatis.hello;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.talk.is.cheap.orm.mybatis.hello.service.BlogService;

import java.util.List;

@SpringBootApplication

@Slf4j
public class Main implements CommandLineRunner {
    public static void main(String[] args) {

        SpringApplication.run(Main.class,args);
    }


    @Autowired
    private BlogService blogService;

    @Override
    public void run(String... args) throws Exception {
        log.info("{}",blogService.selectByIdsMapper(List.of(1,2,3,4,5)));
        log.info("{}",blogService.selectByIdsDao(List.of(1,2,3,4,5)));
    }
}