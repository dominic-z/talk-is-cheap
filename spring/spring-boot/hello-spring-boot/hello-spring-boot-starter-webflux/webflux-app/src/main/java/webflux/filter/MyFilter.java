package webflux.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import webflux.anno.Token;

import java.nio.charset.StandardCharsets;

// 必须成为一个bean才能生效
//@Component
@Slf4j
public class MyFilter implements WebFilter {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        System.out.println("拦截器进来了");


        // 可以校验是否登录
        if (false) {
            ServerHttpResponse response = exchange.getResponse();
            String errorInfo = "{\"code\": 401,\"success\": false}";
            DataBuffer buffer = response.bufferFactory().wrap(errorInfo.getBytes(StandardCharsets.UTF_8));
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            // 注意，这里如果不需要写信息的话，可以直接return response.setComplete();
            return response.writeWith(Mono.just(buffer));
        }


        // 可以拿到ApplicationContext
        ApplicationContext applicationContext = exchange.getApplicationContext();

        // 获取请求对应的HandlerMethod
        Mono<HandlerMethod> handlerMethodMono = requestMappingHandlerMapping
                .getHandler(exchange).cast(HandlerMethod.class);

        boolean[] flags = new boolean[1];

        handlerMethodMono.subscribe(handlerMethod -> {
            log.info("method: {}", handlerMethod.getMethod().toString());
            // 判断Method是否含有对应注解
            boolean flag = handlerMethod.hasMethodAnnotation(Token.class);
            flags[0] = flag;
            if (!flag) return;

            // TODO: 校验Token
            MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
            HttpHeaders headers = exchange.getRequest().getHeaders();

        }).dispose(); // 停止订阅，上游停止生成数据

        if (flags[0]) {
            System.out.println("有注解");
        } else {
            System.out.println("没注解");
        }

        ;
//        exchange.getResponse();
        // 必须要放行才能继续
        return chain.filter(exchange);
    }
}

