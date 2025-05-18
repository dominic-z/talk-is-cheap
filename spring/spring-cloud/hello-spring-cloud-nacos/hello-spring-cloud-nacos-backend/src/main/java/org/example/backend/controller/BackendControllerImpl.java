package org.example.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.message.pojo.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RequestMapping(path = "/backend")
@RestController
@Slf4j
public class BackendControllerImpl implements BackendController {
    @Override
    public ResponseEntity<String> getHello(String name, int id) {

        log.info("name: {}", name);
        ResponseEntity<String> response = new ResponseEntity<String>("hello " + name + " id: " + id,
                HttpStatusCode.valueOf(HttpStatus.OK.value()));

        return response;
    }
}
