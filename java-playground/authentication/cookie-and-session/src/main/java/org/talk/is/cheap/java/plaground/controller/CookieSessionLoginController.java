package org.talk.is.cheap.java.plaground.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.java.plaground.domain.message.LoginRequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/cookie-session")
@Slf4j
public class CookieSessionLoginController {

    @RequestMapping(path = "login",
            method = {RequestMethod.GET, RequestMethod.POST})
    public String login(HttpServletRequest req, HttpServletResponse resp,
                        @RequestBody LoginRequestBody body) {
        log.info("req body, {} ", body);

        if ("abc".equals(body.getUsername()) && "123".equals(body.getPassword())) {
            HttpSession session = req.getSession(true);
            session.setMaxInactiveInterval(20);
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
}
