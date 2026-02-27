package org.talk.is.cheap.project.free.flow.starter.worker.config.filters;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Configuration
public class HttpLoggingFilter implements WebFilter {

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
//                            log.info("开始写入req");
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
//                                log.info("开始写入response");
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

                    if(StringUtils.equals(URIs.WorkerNodeURIs.PING,request.getPath().toString())){
                        // ping这个路径不要打印了
                        log.debug("Method: {}, Path: {}, Request Body: {}, Response Status: {}, Response Body: {}",
                                request.getMethod(),request.getPath(),requestBody,originalResponse.getStatusCode(),responseBody
                        );
                    }else{

                        log.info("Method: {}, Path: {}, Request Body: {}, Response Status: {}, Response Body: {}",
                                request.getMethod(),request.getPath(),requestBody,originalResponse.getStatusCode(),responseBody
                        );
                    }
                });
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...(truncated)";
    }
}
