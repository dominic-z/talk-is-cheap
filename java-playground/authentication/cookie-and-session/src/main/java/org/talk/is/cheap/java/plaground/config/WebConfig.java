package org.talk.is.cheap.java.plaground.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.talk.is.cheap.java.plaground.inceptor.LoginInterceptor;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
//                “/*”下的url都是请求模板的路径，如果将'/*'添加到exclude里之后，最后两个路径是测试cookie和session用的
                .excludePathPatterns("/","/**/login","/**/html","/login/**","/cookie-session-test","/cookie-session/**");
    }
}
