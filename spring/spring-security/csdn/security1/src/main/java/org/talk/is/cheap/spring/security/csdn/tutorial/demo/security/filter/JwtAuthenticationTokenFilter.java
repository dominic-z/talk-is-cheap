package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao.redis.RedisDao;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.model.LoginUser;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.model.User;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JwtUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;

@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private RedisDao redisDao;

    @Override
    /**
     * 这个是判断用户有没有登录的关键，说到底，还是手写的
     */
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("token");
        log.info("method: {}",request.getMethod());
        log.info("cookies: {}", Arrays.toString(request.getCookies()));

//        魔改一下，前后端分离的跨域，如果在header里加上token的话，就会形成非简单请求，就会多个option请求，为了规避这个，我打算把token放在cookie里

        if(request.getCookies()!=null){
            for(Cookie cookie:request.getCookies()){
                if (StringUtils.equals(cookie.getName(),"user")){
                    token = cookie.getValue();
                }
            }
        }

        if (StringUtils.isBlank(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = JwtUtil.parseJWT(token);
            String userId = claims.getSubject();
            User user = redisDao.getCacheObject("login:" + userId, User.class);
            if (user == null) {
                throw new RuntimeException("用户未登录");
            }
            //存入SecurityContextHolder

            LoginUser loginUser = new LoginUser();
            loginUser.setUser(user);
            //TODO 获取权限信息封装到Authentication中
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginUser, null, null);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            //放行
            filterChain.doFilter(request, response);


        }catch (ExpiredJwtException e){
            log.info("jwt 过期",e);
//            jwt过期的异常先忽略
            filterChain.doFilter(request, response);

        }catch (Exception e) {
            log.error("{}",e);
            throw new RuntimeException(e);
        }
    }
}
