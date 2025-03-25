package com.example.websocketdemo.config;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.UUID;

@Configuration
@Slf4j
public class SessionConfig extends ServerEndpointConfig.Configurator {
    /**
     * 注意:
     * 每当一个客户端发起握手,端点就有一个新的实列
     * 那么引用的这个配置也是新的实列 所以内存地址不一样
     * 修改握手机制  就是第一次http发送过来的握手 类似于拦截器，在握手时发生。
     *
     * @param sec      服务器websocket端点的配置
     * @param request
     * @param response
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
//        super.modifyHandshake(sec, request, response);
        HttpSession httpSession = (HttpSession) request.getHttpSession();
//        将从握手的请求中获取httpsession

        /**
         * 一般会在请求头中添加token 解析出来id作为键值对
         */
        Map<String, Object> properties = sec.getUserProperties();
        /**
         * 一个客户端和和服务器发起一次请求交互 就有一个唯一session
         *存储session 是为了能够从其中用户用户info 到时候作为wssession的key 但是session 不共享 为此redis改进 或者token也是可以的
         * 这里使用UUID作为标识
         */
//        properties.put(HttpSession.class.getName(),httpSession);
        String sessionKey = UUID.randomUUID().toString().replaceAll("-", "");
        log.info("configure hashcode: {}, sessionKey: {}", this.hashCode(), sessionKey);
        properties.put("Connected", sessionKey);
    }
}
