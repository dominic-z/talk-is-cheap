package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author dominiczhu
 * @date 2020/8/11 7:57 下午
 */
@SpringBootApplication
// 这个启动类必须与其他restcontroller的包在同一级才管用
public class HelloSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloSpringApplication.class, args);
    }


}
