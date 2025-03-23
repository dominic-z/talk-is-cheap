package websocketdemo.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class EchoWsHandler implements WebSocketHandler {

    // 用来存session的静态对象
    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        // 与传统的WebSocketServer不同，这个Handler对象是单例的。
        log.info("this.hashcode: {}", this.hashCode());
        log.info("uri: {}", session.getUri());
        log.info("attributes: {}", attributes);
        Object counter = attributes.get("counter");
        String id = session.getId();
        SESSIONS.put(id, session);
        log.info("成功建立连接~ id: {}", id);
        session.sendMessage(new BinaryMessage("hello".getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        Object payload = message.getPayload();
        log.info("sessionId: {}, payload: {}", session.getId(), payload);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("error: ", exception);
        if (session.isOpen()) {
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {

        log.info("连接已关闭,sessionId: {}, status:{}", session.getId(), closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 指定发消息
     *
     * @param message
     */
    public static void sendMessage(String id, String message) {
        WebSocketSession webSocketSession = SESSIONS.get(id);
        if (webSocketSession == null || !webSocketSession.isOpen()) return;
        try {

            webSocketSession.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message){
        SESSIONS.keySet().forEach((k)->sendMessage(k,message));
    }
}