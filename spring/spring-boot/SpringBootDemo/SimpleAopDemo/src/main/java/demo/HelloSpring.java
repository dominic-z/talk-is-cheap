package demo;

import demo.controllers.HelloController;
import demo.domain.messages.MyRequest;
import demo.services.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author dominiczhu
 * @date 2020/8/12 6:46 下午
 */
@SpringBootApplication
@Slf4j
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class HelloSpring implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(HelloSpring.class,args);
    }


    @Autowired
    private HelloController helloController;

    @Autowired
    private HelloService helloService;

    @Override
    public void run(String... args) throws Exception {
        log.info("start hello");
        MyRequest myRequest = new MyRequest();
        myRequest.setReqId("123");
        String hello = helloController.hello(myRequest);
        log.info(hello);
        log.info("end hello");


        log.info("start call self");
        helloService.callSelf();
        log.info("end call self");

    }
}
