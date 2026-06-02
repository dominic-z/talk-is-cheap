package org.talk.is.cheap.project.free.flow.scheduler;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableFeignClients
@CrossOrigin(origins = "*", allowCredentials = "true")
public class SchedulerApp {

    public static void main(String[] args) {

//        try {
//            Thread.sleep(new Random().nextInt(1000));
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        SpringApplication.run(SchedulerApp.class, args);
    }
}
