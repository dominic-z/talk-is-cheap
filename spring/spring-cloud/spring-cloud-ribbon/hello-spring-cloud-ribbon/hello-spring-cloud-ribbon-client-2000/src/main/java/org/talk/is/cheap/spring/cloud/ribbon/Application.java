package org.talk.is.cheap.spring.cloud.ribbon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Application
 * @date 2022/3/31 5:28 下午
 */
@SpringBootApplication
@EnableFeignClients
// 得启动eureka客户端，才能使得ribbon的配置生效
@EnableEurekaClient
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
