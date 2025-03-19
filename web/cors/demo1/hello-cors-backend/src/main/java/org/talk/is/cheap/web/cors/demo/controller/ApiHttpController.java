package org.talk.is.cheap.web.cors.demo.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.talk.is.cheap.web.cors.demo.message.Request;
import org.talk.is.cheap.web.cors.demo.message.Response;

@RestController
@RequestMapping("/api")
@Slf4j

public class ApiHttpController {

    // 用来给客户端设置cookie
    // 用来测试Access-Control-Allow-Credentials
    //
    @CrossOrigin("http://localhost:5000")
    @RequestMapping(method = {RequestMethod.POST}, path = "/login")
    @ResponseBody
    public Response<String> login( HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie("test", "test");
        // 设置 Cookie 的有效期（单位：秒），这里设置为 1 小时
        cookie.setMaxAge(3600);
        cookie.setDomain("localhost");

        response.addCookie(cookie);
        return Response.OK("ok");
    }

    @CrossOrigin("http://localhost:5000")
    @RequestMapping(method = RequestMethod.POST, path = "/logout")
    @ResponseBody
    public Response<String> logout(@RequestBody Request req, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie("test", "test");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return Response.OK("ok");
    }


    @RequestMapping(method = RequestMethod.POST, path = "/post1")
    @ResponseBody
    public Response<String> post1(@RequestBody Request req) {
        String data = String.format("hello post1 %s", req.getMessage());
        return Response.OK(data);
    }

    //你会发现加上@CrossOrigin("*")注解，无非就是在响应头上加了一个header，这和在ResponseFilter里实现的是一样的。
//    @CrossOrigin("http://localhost:5000")
    @RequestMapping(method = RequestMethod.POST, path = "/post2")
    @ResponseBody
    public Response<String> post2(@RequestBody Request req) {
        String data = String.format("hello post2 %s", req.getMessage());
        return Response.OK(data);
    }

    @RequestMapping(method = RequestMethod.OPTIONS, path = "/post2")
    @ResponseBody
    public Response<String> post2Option(@RequestBody Request req) {
        String data = String.format("hello post2 %s", req.getMessage());
        return Response.OK(data);
    }

    /**
     * 这种方法是不行的，因为都tm进不来post3的方法里，预检请求就被毙了
     *
     * @param req
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, path = "/post3")
    @ResponseBody
    public Response<String> post3(@RequestBody Request req, HttpServletRequest request, HttpServletResponse response) {
        String data = String.format("hello post3 %s", req.getMessage());
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("origin"));
        response.addHeader("Access-Control-Allow-Headers", "customized");
        response.addHeader("Access-Control-Allow-Headers", "content-type");
        return Response.OK(data);
    }
}
