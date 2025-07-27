package hello.spring.boot3.spi.starter.controller;


import hello.spring.boot3.spi.starter.service.HiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/starter")
@Slf4j
public class StarterControllerImpl implements StarterController {

    /**
     * 经过测试，虽然这个类是通过starter调度autoconfig创建的，并且我也没有手动对这个对象进行set
     * 但这个hiService还是会被正确的设置
     */
    @Autowired
    private HiService hiService;

    @Override
    public String starterHello(@RequestParam(name = "info", required = false, defaultValue = "default") String info) {
        log.info("hiService: {}", hiService);
        hiService.hi();
        log.info("starter hello");
        return "starter " + info;
    }
}
