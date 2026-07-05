package webflux.filter;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 注意：一个打印请求体和响应体的过滤器，和webmvc一样，数据流只能读取一次，
 */
@Configuration
@Slf4j
public class LoggingWebFilter implements WebFilter {


    private static final ThreadPoolExecutor LOG_THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10));
    private static final int MAX_LOG_LENGTH = 2*1024; // 防止大 body 拖垮日志


    private static class LogContext {
        int reqWritePosition;
        byte[] reqCachedBody;

        int respWritePosition;
        byte[] respCachedBody;

        void tryExpandReqCache() {
            if (MAX_LOG_LENGTH <= reqCachedBody.length) {
                return;
            }
            byte[] newCache = new byte[Math.min(MAX_LOG_LENGTH, reqCachedBody.length * 2)];
            System.arraycopy(reqCachedBody, 0, newCache, 0, reqCachedBody.length);
            reqCachedBody = newCache;
        }

        void tryExpandRespCache() {
            if (MAX_LOG_LENGTH <= respCachedBody.length) {
                return;
            }
            byte[] newCache = new byte[Math.min(MAX_LOG_LENGTH, respCachedBody.length * 2)];
            System.arraycopy(respCachedBody, 0, newCache, 0, respCachedBody.length);
            respCachedBody = newCache;
        }
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 1. 缓存并重放请求体
        ServerHttpRequest request = exchange.getRequest();

        LogContext logContext = new LogContext();

        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {


            @Override
            public Flux<DataBuffer> getBody() {
                return super.getBody()
                        .map(dataBuffer -> {
                            // 1️⃣ 提取原生字节数组（纯字节操作，无编码假设）
                            if (logContext.reqCachedBody == null) {
                                if (request.getHeaders().getContentLength() != -1) {
                                    if (request.getHeaders().getContentLength() > MAX_LOG_LENGTH) {
                                        logContext.reqCachedBody = new byte[MAX_LOG_LENGTH];
                                    } else {
                                        logContext.reqCachedBody = new byte[(int) request.getHeaders().getContentLength()];
                                    }
                                } else {
                                    logContext.reqCachedBody = new byte[Math.min(dataBuffer.readableByteCount(), MAX_LOG_LENGTH)];
                                }
                            }
                            if (logContext.reqWritePosition == logContext.reqCachedBody.length) {
                                logContext.tryExpandReqCache();
                            }
                            if (logContext.reqWritePosition < logContext.reqCachedBody.length) {
                                int markedPosition = dataBuffer.readPosition();
                                int readableByteCount = Math.min(dataBuffer.readableByteCount(),
                                        logContext.reqCachedBody.length - logContext.reqWritePosition);
                                dataBuffer.read(logContext.reqCachedBody, logContext.reqWritePosition, readableByteCount);
                                // 不建议直接toString，虽然toString方法并不会更新buffer的readPosition，但是因为reactor中，数据可能是分块来的，不能保证当前的dataBuffer是完整的数据
                                // String chunk = dataBuffer.toString(StandardCharsets.UTF_8);
                                logContext.reqWritePosition += readableByteCount;
                                // 恢复readPosition
                                dataBuffer.readPosition(markedPosition);
                            }
                            return dataBuffer;

                        });
            }
        };
        // 2. 缓存并重放响应体
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                Flux<DataBuffer> cachedFlux = Flux.from(body)
                        .map(dataBuffer -> {
                            // 1️⃣ 提取原生字节数组（纯字节操作，无编码假设）
                            if (logContext.respCachedBody == null) {
                                if (response.getHeaders().getContentLength() != -1) {
                                    if (response.getHeaders().getContentLength() > MAX_LOG_LENGTH) {
                                        logContext.respCachedBody = new byte[MAX_LOG_LENGTH];
                                    } else {
                                        logContext.respCachedBody = new byte[(int) response.getHeaders().getContentLength()];
                                    }
                                } else {
                                    logContext.respCachedBody = new byte[Math.min(dataBuffer.readableByteCount(), MAX_LOG_LENGTH)];
                                }
                            }
                            if (logContext.respWritePosition == logContext.respCachedBody.length) {
                                logContext.tryExpandRespCache();
                            }

                            if (logContext.respWritePosition < logContext.respCachedBody.length) {
                                int markedPosition = dataBuffer.readPosition();
                                int readableByteCount = Math.min(dataBuffer.readableByteCount(),
                                        logContext.respCachedBody.length - logContext.respWritePosition);

                                dataBuffer.read(logContext.respCachedBody, logContext.respWritePosition, readableByteCount);
                                // 不建议直接toString，虽然toString方法并不会更新buffer的readPosition，但是因为reactor中，数据可能是分块来的，不能保证当前的dataBuffer是完整的数据
                                // String chunk = dataBuffer.toString(StandardCharsets.UTF_8);
                                logContext.respWritePosition += readableByteCount;
                                // 恢复readPosition
                                dataBuffer.readPosition(markedPosition);
                            }
                            return dataBuffer;
                        });
                return super.writeWith(cachedFlux);
            }
        };

        // 在 WebFlux filter 链中“替换请求和响应对象”，让后续处理逻辑基于你包装后的版本执行。
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .response(decoratedResponse)
                .build();
        return chain.filter(mutatedExchange)
                .publishOn(Schedulers.fromExecutor(LOG_THREAD_POOL_EXECUTOR)) // subscribeOn 决定“上游从哪里开始跑”，publishOn 决定“从这里开始切线程往下跑”
                .doOnTerminate(() -> {
                    // the simple fact that a Mono emits onNext implies completion
                    // 而doOnNext，相当于处理数据的处理流中插入一个处理步骤，因此并不影响数据流的正常读取。即使某个item被doOnNext了，也后续也会继续被操作。
                    // 因为这是流的最后一步了，所以如果报错了，那么必须自己处理掉
                    try {
                        String requestBody = tryConvertToText(logContext.reqCachedBody, request.getHeaders().getContentType());
                        String responseBody = tryConvertToText(logContext.respCachedBody, response.getHeaders().getContentType());

                        log.info("Method: {}, Path: {}, Request Body: {}, Response Status: {}, Response Body: {}",
                                request.getMethod(), request.getPath(), requestBody, response.getStatusCode(), responseBody
                        );
                    } catch (Exception e) {
                        log.error("", e);
                    }
                });
    }

    private String tryConvertToText(byte[] data, MediaType mediaType) {
        if (isTextType(mediaType)) {
            if (data.length == 0) {
                return "";
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length);
            for (byte b : data) {
//                    因为 UTF-8 的设计保证了所有多字节字符的后续字节都以 10 开头，而换行符 \n (0x0A) 和 \r (0x0D) 的值都小于 0x80，属于单字节 ASCII 范围。这意味着 \n 和 \r 永远不会作为多字节
//                    UTF-8 字符的一部分出现。
//                    空格也一样
                if ('\n' != b && '\r' != b && ' ' != b) {
                    byteBuffer.put(b);
                }
            }
            byteBuffer.flip();

            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
            decoder.reset();
            CharBuffer cb = CharBuffer.allocate(data.length);
            decoder.decode(byteBuffer, cb, true);
            // decoder会尽可能的解码，即使没有有些字符无法完全解码
            // 告诉 decoder：“如果你还有半个字符没拼完，也别等了，报错/替换”
            decoder.flush(cb);
            cb.flip();
            return cb.toString();
        } else {
            return "can't parse to text";
        }

    }

    private boolean isTextType(MediaType type) {
        return type!=null &&
                (type.isCompatibleWith(MediaType.APPLICATION_JSON)
                || type.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)
                || type.isCompatibleWith(MediaType.TEXT_PLAIN)
                || type.isCompatibleWith(MediaType.TEXT_XML));
    }

}
