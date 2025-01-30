package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo.SysUser;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao.MockRedisCache;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JwtUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 通过filter来对登录状态进行拦截，以及识别当前登录的人。
 */
@Configuration
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private MockRedisCache redisCache;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("token");
        if(StringUtils.isBlank(token)){
            filterChain.doFilter(request,response);
            return;
        }
        String userid;
        String username;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            String subject = claims.getSubject();
            String[] split = subject.split(",");
            userid = split[0];
            username = split[1];
        } catch (Exception e) {
            log.error("token 非法",e);
            throw new RuntimeException(e);
        }

        //从redis中获取用户信息
        String redisKey = "login:" + userid;
        SysUser loginUser = redisCache.getCacheObject(redisKey,SysUser.class);
        if(Objects.isNull(loginUser)){
            throw new RuntimeException("用户未登录");
        }

//        教程里是这段，压根没有将Authorities授权给当前用户，导致当前登录的用户在访问后续接口的时候，什么Authorities（授权）都没有
//        访问任何接口都没有权限
//        教程里放在了
//        //存入SecurityContextHolder
//        //TODO 获取权限信息封装到Authentication中
//        UsernamePasswordAuthenticationToken authenticationToken =
//                new UsernamePasswordAuthenticationToken(loginUser,null,null);

//        授予bnt.sysRole.list权限后，并且将这个authenticationToken放在上下文里，在后续访问接口的时候，security就会根据方法的注解来
//        判断当前用户是否有权限访问当前接口，例如，/business/hello有@PreAuthorize("hasAuthority('bnt.sysRole.list')")注解
//        那么当前用户就能访问，这个securityContextHolder上下文仅做用于当前这一个请求
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = List.of(
                new SimpleGrantedAuthority("bnt.sysRole.list"));
        //存入SecurityContextHolder
        //TODO 获取权限信息封装到Authentication中
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser,null,simpleGrantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        //放行
        filterChain.doFilter(request, response);
    }
}
