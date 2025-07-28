package org.talk.is.cheap.hello.spring.openfeign.frontend.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.talk.is.cheap.hello.spring.openfeign.common.message.GenericData;

import java.util.concurrent.CompletableFuture;


/**
 * Fallback对象，用于调用失败的兜底回调，可以直接继承Client类，从而保证方法的签名一致
 * 可以将通过将backend-service关闭进行模拟。
 */
@Component
@Slf4j
public class AsyncHijClientFallback implements AsyncHijClient {
    @Override
    public CompletableFuture<GenericData<String>> asyncHij(GenericData<String> req) {
        log.info("async error, call fallback");
        return CompletableFuture.completedFuture(GenericData.<String>builder()
                .code(200)
                .data("error")
                .msg("error")
                .build());
    }
}
