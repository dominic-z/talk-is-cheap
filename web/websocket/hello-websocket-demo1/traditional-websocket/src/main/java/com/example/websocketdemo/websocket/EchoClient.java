package com.example.websocketdemo.websocket;

import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;


/**
 * java客户端的形式建立ws链接
 */
@ClientEndpoint
@Component
@Slf4j
public class EchoClient {

    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        log.info("WebSocket 连接已经建立。 sessionId:{}", session.getId());
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到服务器消息： {}", message);
    }

    @OnClose
    public void onClose() {
        log.info("WebSocket 连接已经关闭。");
    }

    @OnError
    public void onError(Throwable t) {
        log.error("WebSocket 连接出现错误：", t);
    }

    public void connect(String url) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI(url));
    }

    public void send(String message) throws IOException {
        // todo: 服务端一个message最大10，需要拆分发送
        session.getBasicRemote().sendText(message, true);
    }


    public void connectAndSend(String clientId) throws Exception {
        connect("ws://localhost:8080/echo/" + clientId);
        send("hello server");
    }

    public void close() throws IOException {
        session.close();
    }
}
