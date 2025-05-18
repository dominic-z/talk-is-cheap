package org.example.bff.controller;


import feign.Param;
import org.example.bff.client.BackendClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/frontend")
public class FrontendController {

    @Autowired
    private BackendClient backendClient;

    @GetMapping("/getHello")
    public ResponseEntity<String> getHello(@RequestParam("name") String name,@RequestParam("id") int id) {

        ResponseEntity<String> backendClientHello = backendClient.getHello(name, id);

        ResponseEntity<String> response = new ResponseEntity<String>("frontend "+backendClientHello.getBody(),HttpStatusCode.valueOf(200));

        return  response;
    }
}
