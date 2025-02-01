package org.talk.is.cheap.web.csrf.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MoneyController {


    @RequestMapping("/transfer")
    public String transferMoney(String account,int money) {
        return "给"+account + "转账"+money ;
    }

    @GetMapping("/")
    public String index() {
        return "Home Page";
    }
}





