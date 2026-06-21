package webflux.filter;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;


/**
 * https://www.qianwen.com/share/chat/82079b85eead4ba0a35bb63c1b68b323
 *
 * https://www.qianwen.com/share/chat/32c2a6eb700643df9fa62790fb8a28c6
 *
 * 一个打印请求体和响应体的过滤器，和webmvc一样，数据流只能读取一次，因此使用doOnNext，相当于处理数据之前做一个副作用操作，不影响数据流的正常读取。
 * 即使某个item被doOnNext了，也后续也会继续被操作。
 *
 */
@Configuration
@Slf4j
public class LoggingWebFilter implements WebFilter {


    private static final int MAX_LOG_LENGTH = 10_000; // 防止大 body 拖垮日志

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 1. 缓存并重放请求体
        ServerHttpRequest request = exchange.getRequest();
        AtomicReference<String> requestBodyRef = new AtomicReference<>(); //

        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                return super.getBody()
                        .doOnNext(dataBuffer -> {
                            // 将 DataBuffer 转为字符串（仅用于日志）
                            String chunk = dataBuffer.toString(StandardCharsets.UTF_8);
                            String current = requestBodyRef.get();
                            log.info("开始写入req");
                            if (current == null) {
                                requestBodyRef.set(chunk);
                            } else {
                                requestBodyRef.set(current + chunk);
                            }
                        })
                        .map(buffer -> buffer); // 透传原 buffer
            }
        };

        // 2. 缓存并重放响应体
        ServerHttpResponse originalResponse = exchange.getResponse();
        AtomicReference<String> responseBodyRef = new AtomicReference<>();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<DataBuffer> fluxBody = (Flux<DataBuffer>) body;
                    return super.writeWith(fluxBody
                            .doOnNext(dataBuffer -> {
                                String chunk = dataBuffer.toString(StandardCharsets.UTF_8);
                                String current = responseBodyRef.get();
                                if (current == null) {
                                    responseBodyRef.set(chunk);
                                } else {
                                    responseBodyRef.set(current + chunk);
                                }
                            }));
                } else if (body instanceof Mono) {
                    Mono<DataBuffer> monoBody = (Mono<DataBuffer>) body;
                    return super.writeWith(monoBody
                            .doOnNext(dataBuffer -> {
                                String chunk = dataBuffer.toString(StandardCharsets.UTF_8);
                                String current = responseBodyRef.get();
                                log.info("开始写入response");
                                if (current == null) {
                                    responseBodyRef.set(chunk);
                                } else {
                                    responseBodyRef.set(current + chunk);
                                }
                            }));
                }
                return super.writeWith(body);
            }
        };

        // 3. 打印日志（在请求完成时）
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .response(decoratedResponse)
                .build();
        return chain.filter(mutatedExchange)
                .doOnTerminate(() -> {
                    String requestBody = truncate(requestBodyRef.get(), MAX_LOG_LENGTH);
                    String responseBody = truncate(responseBodyRef.get(), MAX_LOG_LENGTH);

                    log.info("Method: {}, Path: {}, Request Body: {}, Response Status: {}, Response Body: {}",
                            request.getMethod(),request.getPath(),requestBody,originalResponse.getStatusCode(),responseBody
                            );
                });
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...(truncated)";
    }
}
