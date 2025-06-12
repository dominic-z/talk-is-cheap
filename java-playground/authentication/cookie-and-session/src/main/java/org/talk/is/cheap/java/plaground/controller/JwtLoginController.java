package org.talk.is.cheap.java.plaground.controller;

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

@RestController
@RequestMapping("/jwt")
@Slf4j
public class JwtLoginController {

    @Autowired
    private JWTService jwtService;




    @RequestMapping(path = "login", method = {RequestMethod.GET, RequestMethod.POST})
    public String login(HttpServletRequest req, HttpServletResponse resp,
                           @RequestBody LoginRequestBody body) {
        log.info("get token req body, {} ", body);

        if ("abc".equals(body.getUsername()) && "123".equals(body.getPassword())) {
            String jwt = jwtService.getToken("little pig");
//            可以直接将jwt返回给前端，前端自己处理然后存储在localstorage里，也可以直接塞在token里
//            Cookie cookie = new Cookie(JWTService.JWT_COOKIE_NAME, jwt);
//            resp.addCookie(cookie);
//            return "loginSuccess";
            return jwt;
        }
        return "error";
    }

//    没有logout，单纯的jwt完全可以通过客户端实现退出，删除jwt即可。
}
