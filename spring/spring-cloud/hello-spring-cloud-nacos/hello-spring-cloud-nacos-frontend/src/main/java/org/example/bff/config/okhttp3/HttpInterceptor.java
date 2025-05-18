package org.example.bff.config.okhttp3;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class HttpInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        log.info("http intercept");
        // 获取原始请求
        Request originalRequest = chain.request();
        // 构建新的请求，添加自定义请求头
        Request newRequest = originalRequest.newBuilder()
                .header("Custom-Header", "Custom-Value")
                .build();
        // 继续执行请求链
        return chain.proceed(newRequest);
    }
}
