package org.talk.is.cheap.hello.spring.cloud.openfeign.backend.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.hello.spring.openfeign.common.message.GenericData;

import java.util.concurrent.CompletableFuture;

//@RequestMapping(path = "/backend-service")
public interface AsyncHijController {

    @RequestMapping(path = "/async-hij",method = RequestMethod.POST)
    @ResponseBody
    CompletableFuture<GenericData<String>> asyncHij(@RequestBody GenericData<String> req);
}
