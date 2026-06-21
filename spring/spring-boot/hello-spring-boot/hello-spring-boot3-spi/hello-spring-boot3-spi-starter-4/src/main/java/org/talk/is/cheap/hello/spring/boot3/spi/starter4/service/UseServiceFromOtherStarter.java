package org.talk.is.cheap.hello.spring.boot3.spi.starter4.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.hello.spring.boot3.spi.starter3.service.Starter3Service;

@Service
public class UseServiceFromOtherStarter {


    @Autowired
    private Starter3Service service;


    public void useStarter3Service() {
        System.out.println("starter4-UseServiceFromOtherStarter");
        service.serviceFromStarter3();
    }
}
