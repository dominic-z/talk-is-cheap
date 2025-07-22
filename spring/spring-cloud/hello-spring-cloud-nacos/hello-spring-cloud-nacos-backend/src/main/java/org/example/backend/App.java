package org.example.backend;


import lombok.extern.slf4j.Slf4j;
import org.example.backend.config.ConfigFromNacos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients //开启Feign远程调用功能
@EnableDiscoveryClient // 开启服务发现功能
@Slf4j
public class App implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(App.class,args);
    }
    @Autowired
    ConfigFromNacos configFromNacos;

    @Override
    public void run(String... args) throws Exception {



        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    log.info("config1: {}",configFromNacos.getInfo1());
                    log.info("config2: {}",configFromNacos.getInfo2());
                }
            }
        }).start();
    }
}
