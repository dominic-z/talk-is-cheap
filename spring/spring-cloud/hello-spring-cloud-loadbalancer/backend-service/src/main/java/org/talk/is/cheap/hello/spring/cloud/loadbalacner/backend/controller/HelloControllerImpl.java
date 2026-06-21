package org.talk.is.cheap.hello.spring.cloud.loadbalacner.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping(path = "/backend")
@RestController
@Slf4j
public class HelloControllerImpl implements HelloController {


    @Value("${server.port}")
    private int port;

    @Override
    public ResponseEntity<String> getHello(String name, int id) {

        log.info("name: {}", name);
        String respStr = String.format("hello %s id: %d, I am %d",name,id,port);
        ResponseEntity<String> response = new ResponseEntity<String>(respStr,
                HttpStatusCode.valueOf(HttpStatus.OK.value()));

        return response;
    }
}
