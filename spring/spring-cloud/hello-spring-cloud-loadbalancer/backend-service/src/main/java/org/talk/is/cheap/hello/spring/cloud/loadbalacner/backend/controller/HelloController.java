package org.talk.is.cheap.hello.spring.cloud.loadbalacner.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface HelloController {

    @GetMapping(path = "/get-hello")
    ResponseEntity<String> getHello(@RequestParam("myName") String name,@RequestParam("myId") int id);

}
