package org.talk.is.cheap.hello.spring.cloud.openfeign.backend.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.talk.is.cheap.hello.spring.cloud.message.GenericData;

public interface KonnichiwaController {
    @RequestMapping(path = "/konnichiwa",method = RequestMethod.POST)
    GenericData<String> konnichiwa(@RequestBody GenericData<String> reqBody);
}
