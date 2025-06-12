package org.talk.is.cheap.java.plaground.inceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jwt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.talk.is.cheap.java.plaground.service.JWTService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JWTService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("start preHandle, request URL {}", request.getRequestURL().toString());
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String header = headerNames.nextElement();
        }

        // 通过cookie和session的方式来判断是否登录
        HttpSession session = request.getSession(false);
        log.info("session: {}, id: {}", session, session == null ? null : session.getId());
        if (session != null && session.getAttribute("loginName") != null) {
            log.info("already login, username: {}", session.getAttribute("loginName"));
            return true;
        }


        // 通过jwt的方式判断是否处于登录状态
        try {
            String authorization = null;
            if(!StringUtils.isBlank(request.getHeader("Authorization"))){
                authorization = request.getHeader("Authorization");
            }else if (!StringUtils.isBlank(request.getParameter("token"))){
                authorization = request.getParameter("token");
            }

            if (!StringUtils.isBlank(authorization)) {
                String jwtStr = authorization.replaceAll("^Bearer ", "");
                Jwt<?, ?> jwt = jwtService.validateToken(authorization);
                if (!(jwt.getBody() instanceof Claims)) {
                    log.error("jwt body: {}", jwt.getBody());
                    throw new IncorrectClaimException(jwt.getHeader(), null, "jwt.getBody() is not claims");
                }
                Claims claims = ((Claims) jwt.getBody());
                Object loginName = claims.get(JWTService.JWT_LOGIN_NAME);
                if (loginName == null) {
                    throw new IncorrectClaimException(jwt.getHeader(), claims, "loginName is Blank");
                }
                log.info("jwt: {}, loginName: {}", jwt, ((Claims) jwt.getBody()).get(JWTService.JWT_LOGIN_NAME));
                return true;
            }
        } catch (Exception e) {
            log.error("validate token error", e);
        }

        //            必须要加/，
//            否则的话好像是在当前请求下跳转，例如如果请求http://127.0.0.1:8099/cookie-session/session/invalidate，
//            那么跳转的就是http://127.0.0.1:8099/cookie-session/session/login，可以通过浏览器调试模式请求的调用程序链看到
        response.sendRedirect("/login");
        log.info("need login, username: {}", session == null ? null : session.getAttribute("username"));

        return false;
    }


}
