package demo;

import demo.message.RequestWithEnum;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dominiczhu
 * @date 2020/8/11 7:57 下午
 */
@SpringBootApplication
@RestController
public class HelloSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloSpringApplication.class, args);
    }

    @RequestMapping(value = "hello", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    public String hello(@RequestBody RequestWithEnum request) {
        System.out.println(request);
        return "Hello World!";
    }
}
