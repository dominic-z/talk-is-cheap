package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.service;

import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.message.ResponseResult;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.model.User;

public interface LoginService {
    ResponseResult login(User user);


    ResponseResult logout();
}
