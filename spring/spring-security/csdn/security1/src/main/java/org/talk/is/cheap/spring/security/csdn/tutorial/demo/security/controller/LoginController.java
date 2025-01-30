package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.message.ResponseResult;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.model.User;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.service.LoginService;

@RestController
@CrossOrigin("http://localhost:8080/")
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/user/login")
    public ResponseResult login(@RequestBody User user){
        return loginService.login(user);
    }


}