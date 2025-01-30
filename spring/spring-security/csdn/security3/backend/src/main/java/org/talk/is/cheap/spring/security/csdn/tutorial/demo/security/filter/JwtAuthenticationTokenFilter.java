package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.filter;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo.UserDetailsImpl;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao.RedisCache;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao.UserMapper;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.pojo.User;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JwtUtil;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private UserMapper userMapper;


    @Autowired
    private RedisCache redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = token.substring(7);

        String userid;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userid = claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        Integer cacheObject = redisCache.getCacheObject("login:" + userid, Integer.class);
        if (cacheObject == null) {
            throw new RuntimeException("用户名未登录");
        }

        User user = userMapper.selectById(Integer.parseInt(userid));

        if (user == null) {
            throw new RuntimeException("用户名未登录");
        }
//        授予AUTH1的授权权限，从而可以访问hello3接口
//        赋予USER角色，从而可以访问hello1接口，hasAuth和hasRole本质没区别，甚至底层接口调用的都是相同的，这只是设计上的一种区分。
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("AUTH1"),
                new SimpleGrantedAuthority("ROLE_USER"));

        UserDetailsImpl loginUser = new UserDetailsImpl(user);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, authorities);

        // 如果是有效的jwt，那么设置该用户为认证后的用户
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }
}
