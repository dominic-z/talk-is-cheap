package org.talk.is.cheap.project.free.flow.scheduler.config;

import feign.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.stream.Collectors;


/**
 * 在webflux中使用feign，必须得手动创建一个converter，否则报错
 */
@Configuration
public class FeignReactorConfig {

    @Bean
    @ConditionalOnMissingBean
    public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }


    public static class FeignLogLevelConfig{
        @Bean
        public Logger.Level feignLoggerLevel() {
            return Logger.Level.FULL;
        }
    }
}
