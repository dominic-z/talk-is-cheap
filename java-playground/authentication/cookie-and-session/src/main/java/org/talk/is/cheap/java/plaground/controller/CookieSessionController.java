package org.talk.is.cheap.java.plaground.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Random;

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
    @RequestMapping(path = "cookie/set",
            method = {RequestMethod.GET, RequestMethod.POST})
    public String cookieTest(HttpServletRequest req, HttpServletResponse resp) {

        Cookie k1 = new Cookie("k1", "v1");
        k1.setMaxAge(60);
        resp.addCookie(k1);

        Cookie k2 = new Cookie("k2", "v2");
        k2.setMaxAge(60);
        resp.addCookie(k2);

        return "cookie-test";
    }

    /**
     * 第一次getSession的时候会创建这个Session对象并且向会自动往浏览器的cookie里设置一个JSESSION
     * 如JSESSIONID=AEFAF4F848C22DADD3AD353D448452C8
     * 第二次请求的时候，会根据这个JSESSION找到上次创建的、对应的HttpSession
     *
     * @param req
     * @param resp
     * @return
     */
    @RequestMapping(path = "session/set",
            method = {RequestMethod.GET, RequestMethod.POST})
    public String sessionTest(HttpServletRequest req, HttpServletResponse resp) {

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
        return "session/set";
    }

    /**
     * 这个操作会清除一个session
     * @param req
     * @param resp
     * @return
     */
    @RequestMapping(path = "session/invalidate",
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
