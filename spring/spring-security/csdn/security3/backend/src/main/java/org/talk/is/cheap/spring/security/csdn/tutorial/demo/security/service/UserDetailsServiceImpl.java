package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo.UserDetailsImpl;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao.UserMapper;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.pojo.User;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 框架通过这个方法来获取用户信息，用于后续的用户密码状态比较。
     * @param username the username identifying the user whose data is required.
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("没找着这个叫" + username);
        }

        return new UserDetailsImpl(user);
    }
}
