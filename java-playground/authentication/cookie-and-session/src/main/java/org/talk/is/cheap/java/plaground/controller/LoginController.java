package org.talk.is.cheap.java.plaground.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.talk.is.cheap.java.plaground.domain.message.LoginRequestBody;
import org.talk.is.cheap.java.plaground.service.JWTService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    private JWTService jwtService;

    @RequestMapping(path = "demo",
            method = {RequestMethod.GET, RequestMethod.POST})
    public String demo() {
        return "demo";
    }

    @RequestMapping(path = "login",
            method = {RequestMethod.GET, RequestMethod.POST})
    public String login(HttpServletRequest req, HttpServletResponse resp,
                        @RequestBody LoginRequestBody body) {
        log.info("req body, {} ", body);

        if ("abc".equals(body.getUsername()) && "123".equals(body.getPassword())) {
            HttpSession session = req.getSession(true);
            log.info("session id: {}", session.getId());
            session.setAttribute("loginName", body.getUsername());
            return "loginSuccess";
        }
        return "error";
    }

    @RequestMapping(path = "logout", method = {RequestMethod.GET})
    public String logout(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        log.info("log out, session: {}", session);
        if (session == null) {
            log.info("session is null");
            return "logout null";
        }
        Object loginName = session.getAttribute("loginName");
        if (loginName == null) {
            log.info("login name is null");
            return null;
        }
        log.info("session id: {}, log out name {}", session.getId(), loginName);
        session.invalidate();
        return "logout success " + loginName;

    }


    @RequestMapping(path = "token", method = {RequestMethod.GET, RequestMethod.POST})
    public String getToken(HttpServletRequest req, HttpServletResponse resp,
                           @RequestBody LoginRequestBody body) {
        log.info("get token req body, {} ", body);

        if ("abc".equals(body.getUsername()) && "123".equals(body.getPassword())) {
            String jwt = jwtService.getToken("little pig");
            Cookie cookie = new Cookie(JWTService.JWT_COOKIE_NAME, jwt);
            resp.addCookie(cookie);
            return "loginSuccess";
        }
        return "error";
    }

    @RequestMapping(path = "validateToken", method = {RequestMethod.GET, RequestMethod.POST})
    public String validateToken(HttpServletRequest req, HttpServletResponse resp) {
        try {
            Jwt<?, ?> jwt = jwtService.validateToken(req);
            log.info("jwt: {}", jwt);
        } catch (Exception e) {
            log.error("validate token error", e);
            return "error";
        }

        return "success";
    }

}
