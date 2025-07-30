package org.talk.is.cheap.project.free.flow.scheduler;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.Random;

@SpringBootApplication
@EnableFeignClients
public class App {

    public static void main(String[] args) {

        try {
            Thread.sleep(new Random().nextInt(10000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        SpringApplication.run(App.class,args);
    }
}
