package org.talk.is.cheap.hello.spring.openfeign.frontend.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.talk.is.cheap.hello.spring.cloud.message.GenericData;
import org.talk.is.cheap.hello.spring.cloud.openfeign.backend.controller.HijController;


/**
 * Fallback对象，用于调用失败的兜底回调，可以直接继承Client类，从而保证方法的签名一致
 * 可以将通过将backend-service关闭进行模拟。
 */
@Component
@Slf4j
public class HijClientFallback implements HijClient {

    @Override
    public GenericData<String> hij(GenericData<String> req) {
        log.info("error, call fallback");
        return GenericData.<String>builder()
                .code(500)
                .data("error")
                .msg("error")
                .build();
    }

    @Override
    public GenericData<String> slowHij(GenericData<String> req) {
        log.info("error, call fallback");
        return GenericData.<String>builder()
                .code(500)
                .data("slow error")
                .msg("slow error")
                .build();
    }
}
