package on.java.websocket.api;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;

@ServerEndpoint("/echo")
public class EchoServer {

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WebSocket 连接已经建立。");
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("收到客户端消息：" + message);
        session.getBasicRemote().sendText("服务器收到消息：" + message);
    }

    @OnClose
    public void onClose() {
        System.out.println("WebSocket 连接已经关闭。");
    }

    @OnError
    public void onError(Throwable t) {
        System.out.println("WebSocket 连接出现错误：" + t.getMessage());
    }
}