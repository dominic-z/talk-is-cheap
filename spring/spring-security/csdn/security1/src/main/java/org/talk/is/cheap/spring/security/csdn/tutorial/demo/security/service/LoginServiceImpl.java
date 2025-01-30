package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao.redis.RedisDao;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.message.ResponseResult;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.model.LoginUser;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.model.User;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JwtUtil;

import java.util.HashMap;
import java.util.Objects;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RedisDao redisDao;

    @Override
    public ResponseResult login(User user) {
        log.info("loginuser {}",user);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
//        很神奇的是，下面这个方法会调用到UserDetailsServiceImpl，至于是为啥我还没研究。
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("用户名或密码错误");
        }
        //使用userid生成token
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String userId = loginUser.getUser().getId().toString();
        String jwt = JwtUtil.createJWT(userId);
        //authenticate存入redis
        try {
            redisDao.setCacheObject("login:" + userId, user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //把token响应给前端
        HashMap<String, String> map = new HashMap<>();
        map.put("token", jwt);
        return new ResponseResult(200, "登陆成功", map);
    }


    @Override
    public ResponseResult logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userid = loginUser.getUser().getId();
        redisDao.deleteObject("login:" + userid);
        return new ResponseResult(200, "退出成功",null);
    }
}
