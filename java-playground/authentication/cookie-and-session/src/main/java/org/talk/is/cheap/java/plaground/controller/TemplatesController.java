package org.talk.is.cheap.java.plaground.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Controller
@RequestMapping("/")
public class TemplatesController {

    @RequestMapping("index")
    public String indexHtml() {
        return "index";
    }

    @RequestMapping("login")
    public String loginHtmlHtml() {
        return "login";
    }


    @RequestMapping("cookie-session-test")
    public String cookieSessionTest() {
        return "cookie-session-test";
    }
}
