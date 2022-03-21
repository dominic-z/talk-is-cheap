package test;

import demo.config.AppConfig;
import demo.service.MailService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author dominiczhu
 * @version 1.0
 * @title AopPitDemo
 * @date 2021/9/14 下午5:14
 */
public class AopPitDemo {

    @Test
    public void aopPit() {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        MailService mailService = context.getBean(MailService.class);
        System.out.println(mailService.sendMail());
    }
}
