package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo.UserDetailsImpl;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao.RedisCache;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.pojo.User;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JwtUtil;

import java.util.Objects;

@Service
public class UserLoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    public String login(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
                password);

// 从数据库中对比查找，如果找到了会返回一个带有认证的封装后的用户，否则会报错，自动处理。（这里我们假设我们配置的security是基于数据库查找的）
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        if(Objects.isNull(authenticate)){
            throw new RuntimeException("用户名或者密码错误");
        }

// 获取认证后的用户，类型得和UserDetailsServiceImpl的返回结果类型对应上
        UserDetailsImpl userDetails = (UserDetailsImpl) authenticate.getPrincipal();    // 如果强制类型转换报错的话，可以用我们实现的 `UserDetailsImpl` 类中的 getUser 方法了
        try {
            redisCache.setCacheObject("login:"+userDetails.getUser().getId(),userDetails.getUser().getId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String jwt = JwtUtil.createJWT(userDetails.getUser().getId().toString());

        return jwt;
    }
}
