package org.talk.is.cheap.java.plaground.controller;

import io.jsonwebtoken.Jwt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.java.plaground.domain.message.LoginRequestBody;
import org.talk.is.cheap.java.plaground.service.JWTService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
@Slf4j
public class CommentsController {

    @Autowired
    private JWTService jwtService;

    @RequestMapping(path = "/comments",
            method = {RequestMethod.GET, RequestMethod.POST})
    public String comments(HttpServletRequest req, HttpServletResponse resp,
                           RequestEntity<?> entity) {
        log.info("entity: {}", entity);
        return "demo";
    }

}
