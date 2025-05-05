package webflux.handler;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


//@Component
public class BadGlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 1.创建一个响应对象
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 2.根据不同的异常类型返回不同的错误信息和HTTP状态码
        if (ex instanceof ArithmeticException) {
            //2.1数学计算异常
            System.out.println("数学计算异常！");
            String errorInfo = "{\"code\": 400,\"success\": false}";
            DataBuffer buffer = response.bufferFactory().wrap(errorInfo.getBytes(StandardCharsets.UTF_8));
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            // 注意，这里如果不需要写信息的话，可以直接return response.setComplete();
            return response.writeWith(Mono.just(buffer));
        }
        else {
            //2.2其它兜底异常
            System.out.println("系统异常！");
            String errorInfo = "{\"code\": 500,\"success\": false}";
            byte[] bytes = errorInfo.getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            response.getHeaders().set(HttpHeaders.CONTENT_LENGTH,String.valueOf(bytes.length));
            // 注意，这里如果不需要写信息的话，可以直接return response.setComplete();
            return response.writeWith(Mono.just(buffer));
        }
    }
}
