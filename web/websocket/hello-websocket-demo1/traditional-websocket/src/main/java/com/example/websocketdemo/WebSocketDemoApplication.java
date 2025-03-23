package com.example.websocketdemo;

import com.example.websocketdemo.websocket.EchoClient;
import com.example.websocketdemo.websocket.EchoServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class WebSocketDemoApplication implements CommandLineRunner {

    @Autowired
    private EchoServer echoServer;

    @Autowired
    private EchoClient echoClient;

    public static void main(String[] args) {
        SpringApplication.run(WebSocketDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
//        index.html的链接的demo，向index.html发送信息
//        echoServer.sendMessageBySessionId("0",input);
//        echoServer.sendMessageByClientId("1",input);


        echoClient.connectAndSend(input);
    }
}