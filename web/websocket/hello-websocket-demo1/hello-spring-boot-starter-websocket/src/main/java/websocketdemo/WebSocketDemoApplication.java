package websocketdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import websocketdemo.websocket.EchoWsHandler;

import java.util.Scanner;

@SpringBootApplication
public class WebSocketDemoApplication implements CommandLineRunner {

    @Autowired
    private EchoWsHandler echoWsHandler;


    public static void main(String[] args) {
        SpringApplication.run(WebSocketDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
//        index.html的链接的demo，向index.html发送信息
//        EchoWsHandler.sendMessage();
        EchoWsHandler.broadcast(input);


    }
}