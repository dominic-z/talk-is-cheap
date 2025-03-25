package org.websocket.netty;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.websocket.netty.server.NettyServer;

@SpringBootApplication
@Slf4j
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class,args);
        try {
            new NettyServer().start();
        }catch(Exception e) {
            log.error("NettyServerError:",e);
        }
    }
}
