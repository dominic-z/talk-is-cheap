package org.talk.is.cheap.hello.spring.openfeign.frontend.client.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;

public class HijClientConfiguration {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
