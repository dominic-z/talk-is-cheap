package org.talk.is.cheap.distributed.etcd.java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.talk.is.cheap.distributed.etcd.java.service.AdvancedEtcdService;
import org.talk.is.cheap.distributed.etcd.java.service.AdvancedEtcdServiceImpl;
import org.talk.is.cheap.distributed.etcd.java.service.EtcdService;
import org.talk.is.cheap.distributed.etcd.java.service.EtcdServiceImpl;

/**
 * @author dominiczhu
 * @version 1.0
 * @title AdvancedOperateApplication
 * @date 2022/3/27 1:00 下午
 */

@SpringBootApplication
public class AdvancedOperateApplication {

    public static final String endpoints = "http://localhost:2379";


    @Bean
    public EtcdService getEtcdService() {
        return new EtcdServiceImpl(endpoints);
    }

    @Bean
    public AdvancedEtcdService getAdvancedEtcdService() {
        return new AdvancedEtcdServiceImpl(endpoints);
    }

    public static void main(String[] args) {
        SpringApplication.run(AdvancedOperateApplication.class, args);
    }
}
