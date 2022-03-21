package com.example.springboot.hellospringboot.controller;

import com.example.springboot.hellospringboot.domain.messages.RequestMappingRequest;
import com.example.springboot.hellospringboot.domain.messages.RequestMappingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dominiczhu
 * @version 1.0
 * @title RequestMappingController
 * @date 2021/9/14 下午8:40
 */
@RestController
@RequestMapping(path = "/web/anno/requestMapping")
@Slf4j
public class RequestMappingController {

    @RequestMapping(path = "/post", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public RequestMappingResponse post(@RequestBody RequestMappingRequest req) {
        RequestMappingResponse resp = new RequestMappingResponse();
        log.info("/post: req is {}", req.toString());
        resp.setContent(req.getContent());
        return resp;
    }

    // 请求http://localhost/web/anno/requestMapping/post?params=params 如果请求参数有params=params会走到此方法之中
    @RequestMapping(path = "/post",params = "params=params", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public RequestMappingResponse paramPost(@RequestBody RequestMappingRequest req) {
        RequestMappingResponse resp = new RequestMappingResponse();
        log.info("/post with params req is {}", req.toString());
        resp.setContent(req.getContent());
        return resp;
    }

//    http://localhost/web/anno/requestMapping//get?content=hello&value=2
    @RequestMapping(path = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String get(@RequestParam(name = "content") String c,@RequestParam(name = "value") int i) {
        log.info("/get: c is {}, i is {}", c,i);
        return "get done";
    }


}
