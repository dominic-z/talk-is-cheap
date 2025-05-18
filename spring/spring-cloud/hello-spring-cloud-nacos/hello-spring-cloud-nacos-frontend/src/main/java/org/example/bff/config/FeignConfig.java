package org.example.bff.config;


import org.example.bff.config.okhttp3.HttpInterceptor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@AutoConfigureBefore(FeignAutoConfiguration.class)
public class FeignConfig {
/*
配置okhttp-feign
 看FeignAutoCongiguration.OkHttpFeignConfiguration，这个配置类是用于使用okhttp接管feign的配置类，
 不需要完整将全部配置拷贝过来，如果需要对OkHttpClient做定制化，只需要生成一个Builder的bean，去覆盖OkHttpFeignConfiguration默认的builder就可以。
*/
    @Bean
    public okhttp3.OkHttpClient.Builder okHttpClientBuilder(HttpInterceptor httpInterceptor) {
        okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder();
        builder.addInterceptor(httpInterceptor);
        return builder;
    }


}