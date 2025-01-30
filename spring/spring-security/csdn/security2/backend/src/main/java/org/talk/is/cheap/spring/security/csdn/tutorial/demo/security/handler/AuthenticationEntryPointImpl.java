package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JsonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
/**
 * 认证失败的异常，认证失败
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(200);
        int code = 2;
        String msg = "认证失败，无法访问系统资源";
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        result.put("msg", msg);
        result.put("code", code);
        String s = JsonUtil.toJsonString(result);
        response.getWriter().println(s);
    }
}
