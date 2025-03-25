package com.example.websocketdemo.websocket;

import com.example.websocketdemo.config.SessionConfig;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint(value = "/echo/{clientId}", configurator = SessionConfig.class)
@Component
@Slf4j
public class EchoServer {

    /**
     * 静态变量，用来记录当前在线连接数
     */
    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的WebSocket对象。
     */
    private static final ConcurrentHashMap<String, EchoServer> webSocketMap = new ConcurrentHashMap<>();
    // 用来存session的静态对象
    private static final Map<String, Session> sessions = Collections.synchronizedMap(new HashMap<String, Session>());

    private String clientId;
    private Session session;

    // 浏览器打开两个localhost/index.html，模拟两个客户端。
    @OnOpen
    public void onOpen(Session session, @PathParam("clientId") String clientId, EndpointConfig sec) {
        String sessionKey = (String) sec.getUserProperties().get("Connected");
        log.info("Connection opened: session id: {}, client id: {}, configSessionKey: {}", session.getId(), clientId,
                sessionKey);
        sessions.put(session.getId(), session);
        webSocketMap.put(clientId, this);
        // 这里可以发现，每个链接都是一个不同的server对象。不同的config
//        也因此，说明这个实例不是由spring容器管理的
//        @ServerEndpoint注解的类在WebSocket服务器端点中无法直接使用Spring的依赖注入机制，如字段注入（@Autowired和@Resource）或构造器注入。
//        这是因为@ServerEndpoint注解的类是由Java WebSocket API（JSR 356）管理的，而不是由Spring容器直接管理的。
        log.info("endpoint hashcode : {}, config hashcode: {}", this.hashCode(), sec.hashCode());
        this.clientId = clientId;
        this.session = session;

        Map<String, List<String>> query = session.getRequestParameterMap();
        log.info("clientId: {}, query: {}", clientId, query);

        onlineCount.incrementAndGet();
        log.info("客户端连接:" + clientId + ",当前在线人数为:" + onlineCount);
        try {
            session.getBasicRemote().sendText("连接成功，clientId：" + clientId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnMessage(maxMessageSize = 12)
    public void onMessage(Session session, String message, boolean isLast) throws IOException {
        log.info("Received message: {}, isLast: {}", message, isLast);
        session.getBasicRemote().sendText("Server has received: " + message);
        session.getBasicRemote().sendText("Server now send: hello client");
    }

    /**
     * 发生异常调用方法
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误:" + this.clientId + ",原因:" + error.getMessage());
        error.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {
        log.info("Connection closed: sessionId{}, clientId:{}", session.getId(), this.clientId);
        onlineCount.decrementAndGet();
        sessions.remove(session.getId());
        webSocketMap.remove(this.clientId);
    }

    public void sendMessageBySessionId(String sessionId, String message) {
        try {
            sessions.get(sessionId).getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("error ", e);
        }
    }

    public void sendMessageByClientId(String clientId, String message) {
        try {
            webSocketMap.get(clientId).session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("error ", e);
        }
    }

}