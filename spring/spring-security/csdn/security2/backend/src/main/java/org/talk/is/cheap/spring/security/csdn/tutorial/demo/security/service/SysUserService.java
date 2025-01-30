package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo.CustomUser;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo.SysUser;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao.MockRedisCache;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao.SysUserDao;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.message.Result;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JwtUtil;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.vo.LoginVo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class SysUserService {

    @Autowired
    private SysUserDao sysUserDao;

    @Autowired
    private MockRedisCache redisCache;

    @Autowired
    private AuthenticationManager authenticationManager;
    public Optional<SysUser> selectByUsername(String username){
        return sysUserDao.selectUserByUsername(username);
    }


    public Result<Map<String,Object>> login(LoginVo loginVo){
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(loginVo.getUsername(), loginVo.getPassword());
        Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        if(Objects.isNull(authenticate)){
            throw new RuntimeException("用户名或者密码错误");
        }

//        这个返回的为啥是CustomUser，是通过UserDetailsServiceImpl来返回出来的
        CustomUser customUser = (CustomUser) authenticate.getPrincipal();
        String jwt = JwtUtil.createJWT(String.format("%s,%s", customUser.getSysUser().getId(),
                customUser.getSysUser().getUsername()));
        HashMap<String, Object> data = new HashMap<>();
        data.put("token",jwt);

//        登录成功，就把登录状态塞进redis
        try {
            redisCache.setCacheObject(String.format("login:%d",customUser.getSysUser().getId()),customUser.getSysUser());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return Result.ok(data);
    }
}
