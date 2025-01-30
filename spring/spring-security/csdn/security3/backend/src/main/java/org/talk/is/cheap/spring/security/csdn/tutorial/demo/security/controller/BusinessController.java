package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo.UserDetailsImpl;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.message.resp.Result;

@RestController
@RequestMapping("/business")
public class BusinessController {


    @RequestMapping("/hello1")
    public Result<String> hello1(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String data = "hello1 " + userDetails.getUser().getUsername();
        Result<String> stringResult = getResult(data);
        return stringResult;
    }
    @RequestMapping("/hello2")
    public Result<String> hello2(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String data = "hello2 " + userDetails.getUser().getUsername();
        Result<String> stringResult = getResult(data);
        return stringResult;
    }
    @RequestMapping("/hello3")
    public Result<String> hello3(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String data = "hello3 " + userDetails.getUser().getUsername();
        Result<String> stringResult = getResult(data);
        return stringResult;
    }

    private static Result<String> getResult(String data) {
        Result<String> stringResult = new Result<>();
        stringResult.setData(data);
        stringResult.setMessage("");
        stringResult.setCode(0);
        return stringResult;
    }
}
