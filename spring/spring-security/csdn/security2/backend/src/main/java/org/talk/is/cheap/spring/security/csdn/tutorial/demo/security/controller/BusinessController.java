package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.message.Result;

@RequestMapping("/business")
@RestController
@Slf4j

public class BusinessController {


    @GetMapping("/hello")
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    public Result<String> hello(){
        return Result.ok("/hello");
    }


    @GetMapping("/api/hello")
    @PreAuthorize("hasAuthority('some.other.role')")
    public Result<String> apiHello(){
        return Result.ok("/api/hello");
    }

    @GetMapping("/hello/api-docs")
    public Result<String> apiDocHello(){
        return Result.ok("/hello/api-docs");
    }
}
