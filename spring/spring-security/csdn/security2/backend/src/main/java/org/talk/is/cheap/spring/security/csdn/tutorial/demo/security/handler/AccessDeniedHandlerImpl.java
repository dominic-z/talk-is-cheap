package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JsonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 没权限时候的handler，授权不够
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        int code = 3;
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        String msg = "权限不足，无法访问系统资源";
        Map<String, Object> result = new HashMap<>();
        result.put("msg", msg);
        result.put("code", code);
        String s = JsonUtil.toJsonString(result);
        response.getWriter().println(s);
    }
}
