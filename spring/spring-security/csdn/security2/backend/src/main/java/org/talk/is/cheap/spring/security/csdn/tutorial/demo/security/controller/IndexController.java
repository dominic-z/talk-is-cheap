package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.message.Result;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.service.SysUserService;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.vo.LoginVo;

import java.util.Map;

@RequestMapping("/admin/system/index")
@RestController
@Slf4j
public class IndexController {

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    public Result<Map<String,Object>> login(@RequestBody LoginVo loginVo){
        return sysUserService.login(loginVo);
    }
}
