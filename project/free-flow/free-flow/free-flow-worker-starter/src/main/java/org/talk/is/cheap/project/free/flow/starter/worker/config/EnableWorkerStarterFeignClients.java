package org.talk.is.cheap.project.free.flow.starter.worker.config;


import org.springframework.cloud.openfeign.EnableFeignClients;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerClusterClient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 废弃
 * 原来是希望主应用挂一个@EnableWorkerStarterFeignClients，但后来发现，在AutoConfig上挂就好了
 */
//@EnableFeignClients(clients = {SchedulerClusterClient.class})
//@Retention(RetentionPolicy.RUNTIME)
//@Target(ElementType.TYPE)
//public @interface EnableWorkerStarterFeignClients {
//}
