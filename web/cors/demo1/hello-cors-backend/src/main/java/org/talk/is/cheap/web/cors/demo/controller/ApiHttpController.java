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

import java.util.Arrays;

@RestController
@RequestMapping("/api")
@Slf4j

public class ApiHttpController {

    // 用来给客户端设置cookie从而测试Access-Control-Allow-Credentials
    // 一篇文章彻底解决跨域设置cookie问题！https://cloud.tencent.com/developer/article/2207110
    // 但是，跨域的时候setcookie无法简单的生效，tmd，需要配置https和SameSite属性，见上文
    // 一个偷懒的做法，启动另一个项目，给localhost下设置一个cookie，然后再在这个项目里用这个cookie
    @CrossOrigin(origins = "http://localhost:5000",allowCredentials = "true")
    @RequestMapping(method = {RequestMethod.POST}, path = "/login")
    @ResponseBody
    public Response<String> login( HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie("test", "test");
        // 设置 Cookie 的有效期（单位：秒），这里设置为 1 小时
        cookie.setMaxAge(3600);
        cookie.setDomain("localhost");

        response.addCookie(cookie);
//        response.setHeader("set-cookie","test=test; Max-Age=3600; Expires=Thu, 12 Jun 2025 15:57:37 GMT; Domain=localhost; SameSite=None");

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


    // 通过filter的方式手动给response添加上允许跨域的header
    @RequestMapping(method = RequestMethod.POST, path = "/post1")
    @ResponseBody
    public Response<String> post1(@RequestBody Request req) {
        String data = String.format("hello post1 %s", req.getMessage());
        return Response.OK(data);
    }

    //加上CrossOrigin相当于在响应中加上了access-control-allow-origin，但是比较奇怪的是预检请求不见了
    @CrossOrigin("http://localhost:5000")
    @RequestMapping(method = RequestMethod.POST, path = "/post2")
    @ResponseBody
    public Response<String> post2(HttpServletRequest request, HttpServletResponse response) {
        String data = String.format("hello post2 %s", "a");
        return Response.OK(data);
    }

    // 下面这东西不管用
//    @CrossOrigin("http://localhost:5000")
//    @RequestMapping(method = RequestMethod.OPTIONS, path = "/post2")
//    @ResponseBody
//    public Response<?> post2Option() {
////        String data = String.format("hello post2 %s", req.getMessage());
//        return Response.OK("");
//    }

    /**
     * 尝试手动在restcontroller里添加请求头，但这种方法是不行的，因为都tm进不来post3的方法里，预检请求就被毙了
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
