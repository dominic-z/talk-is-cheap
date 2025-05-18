package org.example.backend;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients //开启Feign远程调用功能
@EnableDiscoveryClient // 开启服务发现功能
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class,args);
    }
}
