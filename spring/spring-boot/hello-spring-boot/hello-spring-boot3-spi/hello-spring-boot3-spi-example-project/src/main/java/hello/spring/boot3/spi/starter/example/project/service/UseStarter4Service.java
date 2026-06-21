package hello.spring.boot3.spi.starter.example.project.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.hello.spring.boot3.spi.starter3.service.Starter3Service;
import org.talk.is.cheap.hello.spring.boot3.spi.starter4.service.UseServiceFromOtherStarter;

@Service
public class UseStarter4Service {

    @Autowired
    private UseServiceFromOtherStarter useServiceFromOtherStarter;

    public void useOtherService(){
        useServiceFromOtherStarter.useStarter3Service();
    }
}
