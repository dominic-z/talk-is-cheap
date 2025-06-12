package org.talk.is.cheap.java.plaground.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;


/**
 * cookie和session的测试controller
 */
@RestController
@Slf4j
@RequestMapping("cookie-session")
public class CookieSessionController {


    /**
     * 手动设置一个cookie，在浏览器里可以看到cookie里就会看到k1=v1;k2=v2
     *
     * @param req
     * @param resp
     * @return
     */
    @RequestMapping(path = "set-cookie",
            method = {RequestMethod.GET, RequestMethod.POST},produces = {MediaType.TEXT_PLAIN_VALUE})
    public String setCookie(RequestEntity<?> body, HttpServletRequest req, HttpServletResponse resp) {
        log.info("post body :{}",body);
        Cookie k1 = new Cookie("k1", "v1");
        k1.setDomain("localhost");
        k1.setMaxAge(60);
        resp.addCookie(k1);

        Cookie k2 = new Cookie("k2", "v2");
        k2.setMaxAge(60);
        k2.setDomain("localhost");
        resp.addCookie(k2);

        return "set-cookie";
    }

    @RequestMapping(path = "read-cookie",
            method = {RequestMethod.GET, RequestMethod.POST})
    public String readCookie(HttpServletRequest req, HttpServletResponse resp) {

        log.info("cookies: {}", Arrays.stream(req.getCookies())
                .map(cookie -> String.format("k:%s,v:%s",cookie.getName(),cookie.getValue()))
                .collect(Collectors.joining(";"))
        );

        return "read-cookie";
    }
    @RequestMapping(path = "clear-cookie",
            method = {RequestMethod.GET, RequestMethod.POST})
    public String clearCookie(HttpServletRequest req, HttpServletResponse resp){
        Cookie k1 = new Cookie("k1", "v1");
        k1.setDomain("localhost");
        k1.setMaxAge(0);
        resp.addCookie(k1);

        Cookie k2 = new Cookie("k2", "v2");
        k2.setMaxAge(0);
        k2.setDomain("localhost");
        resp.addCookie(k2);

        return "clear-cookie";
    }

    /**
     * 第一次getSession的时候会创建这个Session对象并且向会自动往浏览器的cookie里设置一个JSESSION
     * 响应体里会有一个set-cookie:
     * JSESSIONID=864FD4E73F41F4E784BB4D48307957DD; Path=/; HttpOnly
     * 如JSESSIONID=AEFAF4F848C22DADD3AD353D448452C8
     * 第二次请求的时候，会根据这个JSESSION找到上次创建的、对应的HttpSession
     * 其实就相当于httpservelet封窗好了一个根据cookie查询session的工具。
     * @param req
     * @param resp
     * @return
     */
    @RequestMapping(path = "set-session",
            method = {RequestMethod.GET, RequestMethod.POST})
    public String setSession(HttpServletRequest req, HttpServletResponse resp) {

        HttpSession session = req.getSession();
        session.setMaxInactiveInterval(20);

        log.info("sessionId: {}", session.getId());

        Object v1 = session.getAttribute("k1");
        log.info("v1: {}", v1);
        if (v1 == null) {
            double v = new Random().nextDouble();
            log.info("set v1: {}", v);
            session.setAttribute("k1", v);
        }
        return "set-session";
    }

    @RequestMapping(path = "read-session",
            method = {RequestMethod.GET, RequestMethod.POST})
    public String readSession(HttpServletRequest req, HttpServletResponse resp) {

        HttpSession session = req.getSession();
        session.setMaxInactiveInterval(20);

        log.info("sessionId: {}", session.getId());

        Object v1 = session.getAttribute("k1");
        log.info("v1: {}", v1);
        return "set-session";
    }

    /**
     * 这个操作会清除一个session
     * @param req
     * @param resp
     * @return
     */
    @RequestMapping(path = "clear-session",
            method = {RequestMethod.GET, RequestMethod.POST})
    public String invalidateSession(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            log.info("session is null");
            return "session is null";
        }
        log.info("sessionId: {}", session.getId());

        Object v1 = session.getAttribute("k1");
        log.info("v1: {}", v1);
        session.invalidate();

        return "session is invalidated";
    }
}
