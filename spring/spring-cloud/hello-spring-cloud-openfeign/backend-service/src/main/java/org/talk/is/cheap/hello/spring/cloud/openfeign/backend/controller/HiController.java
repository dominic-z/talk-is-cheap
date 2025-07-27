package org.talk.is.cheap.hello.spring.cloud.openfeign.backend.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.talk.is.cheap.hello.spring.cloud.message.GenericData;

public interface HiController {

    @RequestMapping(path = "/hi",method = RequestMethod.POST)
    GenericData<String> hi(@RequestBody GenericData<String> reqBody);

    @RequestMapping(path = "/get-hi",method = RequestMethod.GET)
    GenericData<String> getHi(@RequestParam(name = "data") String data,@RequestParam(name = "msg") String msg);
}
