package org.talk.is.cheap.web.csrf.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class TransferController {

    @RequestMapping("/transferpage")
    public String transferPage() {
        return "transferpage";
    }
}