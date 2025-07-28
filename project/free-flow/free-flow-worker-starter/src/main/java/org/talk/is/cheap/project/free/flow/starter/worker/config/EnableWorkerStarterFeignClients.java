package org.talk.is.cheap.project.free.flow.starter.worker.config;


import org.springframework.cloud.openfeign.EnableFeignClients;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerClusterClient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@EnableFeignClients(clients = {SchedulerClusterClient.class})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableWorkerStarterFeignClients {
}
