package org.talk.is.cheap.web.cors.demo.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@Slf4j
public class ResponseFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("request origin {}", request.getHeader("origin"));
        if (StringUtils.equals(request.getRequestURI(), "/api/post1")) {
//        加上这个就是相当于允许跨域了。
//            对于复杂请求，浏览器会首先发送一个 OPTIONS 请求，包含以下头部字段：
//
//            Origin：指示请求的源。
//
//            Access-Control-Request-Method：指示实际请求将使用的方法。
//
//            Access-Control-Request-Headers：指示实际请求将包含的自定义头部。

//            原文链接：https://blog.csdn.net/leah126/article/details/141624726
            log.info("req method: {}", request.getMethod());
            response.addHeader("Access-Control-Allow-Origin", request.getHeader("origin"));
            response.addHeader("Access-Control-Allow-Headers", "customized");
            response.addHeader("Access-Control-Allow-Headers", "content-type");
        }

        filterChain.doFilter(request, response);

//       临时的测试，会在尾巴上新增一个someting，可以通过postman看出来
//        response.getOutputStream().write("something".getBytes());
//        如果已经完成了doFilter，那么此时对response就无法操作了。
//        log.info("you can debug here and get response");
    }
}
