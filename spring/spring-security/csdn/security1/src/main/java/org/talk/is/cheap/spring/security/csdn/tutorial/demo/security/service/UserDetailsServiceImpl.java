package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao.mysql.UserMapper;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.model.LoginUser;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.model.User;

import java.util.Objects;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 实际进行密码比对的是DaoAuthenticationProvider这个类的additionalAuthenticationChecks方法
     * 这里使用了pwencoder对输入密码进行编码，然后与数据库进行比对
     * @param username the username identifying the user whose data is required.
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //根据用户名查询用户信息
        User user = userMapper.selectUserByUsername(username);
        //如果查询不到数据就通过抛出异常来给出提示
        if(Objects.isNull(user)){
            throw new RuntimeException("用户名或密码错误，不存在这玩意");
        }
        //TODO 根据用户查询权限信息 添加到LoginUser中

        //封装成UserDetails对象返回
        return new LoginUser(user);
    }
}