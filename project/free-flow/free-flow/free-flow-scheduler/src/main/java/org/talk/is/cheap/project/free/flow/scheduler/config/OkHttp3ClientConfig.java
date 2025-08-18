package org.talk.is.cheap.project.free.flow.scheduler.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class OkHttp3ClientConfig {

//    OkHttpFeignConfiguration这个配置类里，其实只暴露了okHttpClientBuilder一个bean，只有这个bean有ConditionalOnMissingBean
    @Bean
    public okhttp3.OkHttpClient.Builder okHttpClientBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.callTimeout(5000, TimeUnit.MILLISECONDS);
        builder.writeTimeout(3000, TimeUnit.MILLISECONDS);
        return builder;
    }
}
