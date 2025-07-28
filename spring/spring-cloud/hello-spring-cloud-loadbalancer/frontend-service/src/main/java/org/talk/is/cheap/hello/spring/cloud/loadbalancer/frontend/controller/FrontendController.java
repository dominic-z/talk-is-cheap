package org.talk.is.cheap.hello.spring.cloud.loadbalancer.frontend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.hello.spring.cloud.loadbalancer.frontend.client.HelloClient;

@RestController
@RequestMapping(path = "/frontend")
public class FrontendController {

    @Autowired
    private HelloClient helloClient;


    @GetMapping("/get-hello")
    public ResponseEntity<String> getHello(@RequestParam("name") String name, @RequestParam("id") int id) {

        ResponseEntity<String> hello = helloClient.getHello(name, id);

        ResponseEntity<String> response = new ResponseEntity<String>("frontend " + hello.getBody(), HttpStatusCode.valueOf(200));

        return response;
    }


//    @GetMapping("/get-hi")
//    public ResponseEntity<String> getHi(@RequestParam("name") String name, @RequestParam("id") int id) {
//
//        ResponseEntity<String> hi = helloClient.getHi(name, id);
//
//        ResponseEntity<String> response = new ResponseEntity<String>("frontend " + hi.getBody(), HttpStatusCode.valueOf(200));
//
//        return response;
//    }
}
