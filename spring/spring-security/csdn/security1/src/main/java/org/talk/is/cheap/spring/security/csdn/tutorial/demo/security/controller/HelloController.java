package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("http://localhost:8080/")
@RequestMapping("/api")
public class HelloController {

    @RequestMapping("/hello")
    public String hello(){
        return "hello";
    }


    @RequestMapping("/someContent")
    public String someContent(){
        return "someContent";
    }

}