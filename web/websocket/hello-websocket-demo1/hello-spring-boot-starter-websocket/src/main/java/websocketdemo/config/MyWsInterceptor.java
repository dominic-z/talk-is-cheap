package websocketdemo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.ExceptionWebSocketHandlerDecorator;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

//@Configuration
@Slf4j
public class MyWsInterceptor implements HandshakeInterceptor {
    //前置拦截一般用来注册用户信息，绑定 WebSocketSession

    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info("前置拦截~~，{}, handler: {}", request.getClass(),wsHandler);
        log.info("real handler: {}", ((ExceptionWebSocketHandlerDecorator) wsHandler).getDelegate());
        if (!(request instanceof ServletServerHttpRequest)) {
            return true;
        }
//            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
//            String userName = (String) servletRequest.getSession().getAttribute("userName");
        attributes.put("counter", counter.incrementAndGet());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        log.info("后置拦截~~");
    }

}
