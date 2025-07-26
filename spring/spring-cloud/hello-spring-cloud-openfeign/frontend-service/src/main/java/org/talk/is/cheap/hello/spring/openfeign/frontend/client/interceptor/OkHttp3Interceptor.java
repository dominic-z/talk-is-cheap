package org.talk.is.cheap.hello.spring.openfeign.frontend.client.interceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
public class OkHttp3Interceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        log.info("okhttp interceptor");
        Request request = chain.request();
        Response proceed = chain.proceed(request);
        return proceed;
    }
}
