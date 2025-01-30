package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.message.req.LoginReq;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.message.resp.Result;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.service.UserLoginService;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserLoginService loginService;

    @RequestMapping("/loginByPassword")
    public Result<String> login(@RequestBody LoginReq req) {
        String token = loginService.login(req.getUsername(), req.getPassword());

        Result<String> result = new Result<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(token);
        return result;
    }
}
