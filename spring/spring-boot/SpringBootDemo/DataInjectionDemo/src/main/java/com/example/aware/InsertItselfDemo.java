package com.example.aware;

import com.example.components.Apple;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @title ApplicationAwareDemo
 * @Author dominiczhu
 * @Date 2020/12/22 下午2:18
 * @Version 1.0
 */
@Component("insertItself")
@Data
public class InsertItselfDemo implements ApplicationContextAware {
    private ApplicationContext ac;
    private InsertItselfDemo self;

    @Value("name")
    private String name;

    @Autowired
    @Qualifier("apple")
    private Apple apple;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ac = applicationContext;
    }


    @PostConstruct
    public void insertItself() {
        this.setSelf(this.getAc().getBean("insertItself", InsertItselfDemo.class));
        System.out.println(self.apple);
    }

    public void sayHello() {
        System.out.println("hello~!");
        self.sayHelloQuite();
    }

    private void sayHelloQuite() {
        System.out.println("hello~");
        System.out.println("apple is " + apple);
    }
}
