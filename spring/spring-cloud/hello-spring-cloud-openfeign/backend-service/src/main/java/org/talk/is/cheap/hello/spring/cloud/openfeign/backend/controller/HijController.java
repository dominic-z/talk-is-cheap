package org.talk.is.cheap.hello.spring.cloud.openfeign.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.talk.is.cheap.hello.spring.openfeign.common.message.GenericData;

//@RequestMapping(path = "/backend-service")
public interface HijController {

    @RequestMapping(path = "/hij",method = RequestMethod.POST)
    @ResponseBody
    GenericData<String> hij(@RequestBody GenericData<String> req);


    @RequestMapping(path = "/slow-hij",method = RequestMethod.POST)
    @ResponseBody
    GenericData<String> slowHij(@RequestBody GenericData<String> req);

}
