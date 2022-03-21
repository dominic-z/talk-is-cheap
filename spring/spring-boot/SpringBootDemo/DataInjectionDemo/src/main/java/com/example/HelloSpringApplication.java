package com.example;

import com.example.config.SpringContextConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author dominiczhu
 * @date 2020/8/12 10:19 上午
 */

@SpringBootApplication // 该注解自带了componentScan
@ComponentScan(basePackages = {"somewhere.service","com.example"})
public class HelloSpringApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(HelloSpringApplication.class,args);
    }

    @Autowired
    private SpringContextConfig springContextConfig;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(springContextConfig.getApplicationContext().getBean("argApple"));

    }
}
