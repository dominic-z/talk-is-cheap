package org.talk.is.cheap.java.plaground.inceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.talk.is.cheap.java.plaground.service.JWTService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;

@Slf4j
@Component
public class CookieSessionInterceptor implements HandlerInterceptor {

    @Autowired
    private JWTService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("start preHandle, request URL {}", request.getRequestURL().toString());
        Jwt<?, ?> jwt;
        try {
            jwt = jwtService.validateToken(request);
            log.info("jwt: {}, loginName: {}", jwt, ((Claims) jwt.getBody()).get(JWTService.JWT_LOGIN_NAME));
            return true;
        } catch (Exception e) {
            log.error("validate token error", e);
        }

        HttpSession session = request.getSession(false);
        log.info("session: {}, id: {}", session, session == null ? null : session.getId());
        if (session != null && session.getAttribute("username") != null) {
            log.info("already login, username: {}", session.getAttribute("username"));
            return true;
        }
        //            必须要加/，
//            否则的话好像是在当前请求下跳转，例如如果请求http://127.0.0.1:8099/cookie-session/session/invalidate，
//            那么跳转的就是http://127.0.0.1:8099/cookie-session/session/login，可以通过浏览器调试模式请求的调用程序链看到
        response.sendRedirect("/login");
        log.info("need login, username: {}", session == null ? null : session.getAttribute("username"));

        return false;
    }


}
