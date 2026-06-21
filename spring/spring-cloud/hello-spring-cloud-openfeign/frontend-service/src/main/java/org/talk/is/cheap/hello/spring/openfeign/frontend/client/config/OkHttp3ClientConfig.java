package org.talk.is.cheap.hello.spring.openfeign.frontend.client.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talk.is.cheap.hello.spring.openfeign.frontend.client.interceptor.OkHttp3Interceptor;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class OkHttp3ClientConfig {

//    OkHttpFeignConfiguration这个配置类里，其实只暴露了okHttpClientBuilder一个bean，只有这个bean有ConditionalOnMissingBean
    @Bean
    public okhttp3.OkHttpClient.Builder okHttpClientBuilder(@Autowired OkHttp3Interceptor okHttp3Interceptor) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(okHttp3Interceptor);
//        配置完整请求的超时时间。
//        关于callTimeout的说明：The call timeout spans the entire call: resolving DNS, connecting, writing the request body, server processing, and reading the response body. If the call requires redirects or retries all must complete within one timeout period.
//        额，甚至学到了一点okhttp关于超时的定义，注意这里server processing, and reading the response body是不同的时间，readingTimeout指的是开始读取到读取结束的时间，也就是说服务端开始返回结果到完整读取完成
        builder.callTimeout(15000, TimeUnit.MILLISECONDS);
        builder.writeTimeout(3000, TimeUnit.MILLISECONDS);
        return builder;
    }
}
