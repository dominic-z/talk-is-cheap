package com.example.springboot.hellospringboot.config;

import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @date 2020/8/12 10:47 上午
 */
@Component
@Data
public class ApplicationContextConfig implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
