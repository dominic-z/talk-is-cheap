package org.talk.is.cheap.spring.cloud.sleuth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ExampleApplication
 * @date 2022/3/29 1:16 下午
 */

@SpringBootApplication
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients
public class ExampleApplication {



    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }


}
