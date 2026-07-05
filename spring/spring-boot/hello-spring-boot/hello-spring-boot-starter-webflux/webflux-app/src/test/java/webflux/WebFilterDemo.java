package webflux;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import webflux.filter.LoggingWebFilter;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class WebFilterDemo {

    @Test
    void loggingWebFilterCanHandlePostTextBodySplitIntoTwoDataBuffers() {
        String requestText = "你好，WebFlux流式请求";
        byte[] requestBytes = requestText.getBytes(StandardCharsets.UTF_8);
        int splitPosition = 1;

        DataBuffer firstPart = DefaultDataBufferFactory.sharedInstance.wrap(
                Arrays.copyOfRange(requestBytes, 0, splitPosition));
        DataBuffer secondPart = DefaultDataBufferFactory.sharedInstance.wrap(
                Arrays.copyOfRange(requestBytes, splitPosition, requestBytes.length));

        MockServerHttpRequest request = MockServerHttpRequest.post("/logging-demo")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(requestBytes.length)
                .body(Flux.just(firstPart, secondPart));
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<String> downstreamRequestBody = new AtomicReference<>();
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingWebFilter.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        /*
[WebFilterDemo.java:52](/home/dominiczhu/Coding/talk-is-cheap/spring/spring-boot/hello-spring-boot/hello-spring-boot-starter-webflux/webflux-app/src/test/java/webflux/WebFilterDemo.java:52) 里的 `WebFilterChain chain` 是测试里手写的“下游处理链”。

在真实 WebFlux 请求里，`LoggingWebFilter` 执行完之后会调用：

```java
chain.filter(mutatedExchange)
```

这个 `chain` 后面通常还会有其他 filter，最后到 controller/handler。单元测试里没有真实 Spring 容器和 controller，所以我用一个 lambda 假装“后面的业务处理逻辑”。

这个测试里的 `chain` 做了几件事：

1. 从 `filteredExchange.getRequest().getBody()` 读取请求体。
2. 用 `DataBufferUtils.join(...)` 把两个分段传输的 `DataBuffer` 合并成一个完整 `DataBuffer`。
3. 读取字节并按 UTF-8 转回字符串，保存到 `downstreamRequestBody`，用于断言下游是否还能读到完整 body。
4. 设置响应状态为 `200 OK`。
5. 设置响应类型为 `text/plain`。
6. 写入响应体 `"OK"`。

所以它本质上模拟的是一个 controller：

```java
@PostMapping("/logging-demo")
Mono<String> demo(@RequestBody String body) {
    // 断言 body 是完整的
    return Mono.just("OK");
}
```

这个 `chain` 的关键作用是验证 `LoggingWebFilter` 没有把请求体“消费掉”。如果你的 filter 读取请求体后没有正确恢复 `readPosition` 或没有把 body 继续传下去，那么这里的 `chain` 就读不到完整的 `你好，WebFlux流式请求`，测试会失败。

         */

        // 可以看到在loggingWebFilter里执行完request的读取之后，就会进入到这个webFilter里，这里执行完成之后，就会进入response的读取，整体就是过滤器的执行流程
        // 另外，这里的dataBuffer是完整的，因为这里执行了join，将body聚合成了一个dataBuffer
        WebFilterChain chain = filteredExchange -> DataBufferUtils.join(filteredExchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    downstreamRequestBody.set(new String(bytes, StandardCharsets.UTF_8));

                    byte[] responseBytes = "OK".getBytes(StandardCharsets.UTF_8);
                    filteredExchange.getResponse().setStatusCode(HttpStatus.OK);
                    filteredExchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
                    return filteredExchange.getResponse().writeWith(Mono.just(
                            filteredExchange.getResponse().bufferFactory().wrap(responseBytes)));
                });

        LoggingWebFilter filter = new LoggingWebFilter();

        assertThatCode(() -> filter.filter(exchange, chain).block(Duration.ofSeconds(3)))
                .doesNotThrowAnyException();

        assertThat(downstreamRequestBody.get()).isEqualTo(requestText);
        assertThat(exchange.getResponse().getBodyAsString().block(Duration.ofSeconds(3)))
                .isEqualTo("OK");
        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anySatisfy(message -> assertThat(message)
                        .contains("Method: POST")
                        .contains("Path: /logging-demo")
                        .contains("Request Body: " + requestText)
                        .contains("Response Status: 200 OK")
                        .contains("Response Body: OK"));

        logger.detachAppender(listAppender);
    }

}
