package hello.spring.boot3.spi.starter.starter.listener;

import hello.spring.boot3.spi.starter.starter.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;


/**
 * 应用已经启动了，可以使用log对象了
 */
@Slf4j
public class MyApplicationStartedEventListener implements ApplicationListener<ApplicationStartedEvent> {

    /**
     * 这个注入不一定会成功，取决于当前这个对象会不会作为一个bean被管理：
     * 1. 如果这个类在org.springframework.boot.autoconfigure.AutoConfiguration.imports指定的config里创建并作为一个bean了，那么这个对象会自动注入
     * 2. 如果这个类只在spring.factories的org.springframework.context.ApplicationListener指定了，那么这个bean不会被注入，需要从event里的容器里手动注入。
     */
    @Autowired
    private HelloService helloService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (this.helloService == null) {
            log.info("======================从容器里手动设置bean");
            this.helloService = event.getApplicationContext().getBean(HelloService.class);
        } else {
            log.info("======================helloService已经被自动注入");
        }
        this.helloService.sayHello();
    }
}