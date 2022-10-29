package org.talk.is.cheap.java.plaground.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.talk.is.cheap.java.plaground.inceptor.CookieSessionInterceptor;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CookieSessionInterceptor cookieSessionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cookieSessionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/","/**/login","/**/html","/login/**");
    }
}
