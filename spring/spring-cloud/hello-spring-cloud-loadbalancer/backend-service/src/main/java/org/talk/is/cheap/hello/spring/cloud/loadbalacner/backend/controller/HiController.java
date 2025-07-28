package org.talk.is.cheap.hello.spring.cloud.loadbalacner.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface HiController {

    @GetMapping(path = "/get-hi")
    ResponseEntity<String> getHi(@RequestParam("myName") String name,@RequestParam("myId") int id);

}
