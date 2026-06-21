package org.talk.is.cheap.project.free.example;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerClusterInternalClient;

@SpringBootApplication
@EnableFeignClients

public class App {

    @Autowired
    private SchedulerClusterInternalClient schedulerClusterClient;
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}