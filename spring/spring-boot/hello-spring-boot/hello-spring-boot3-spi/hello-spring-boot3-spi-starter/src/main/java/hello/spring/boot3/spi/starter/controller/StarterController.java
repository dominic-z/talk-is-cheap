package hello.spring.boot3.spi.starter.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/starter")
@Slf4j
public class StarterController {


    @GetMapping(path = "/hello")
    @ResponseBody
    public String starterHello(@RequestParam(name = "info",required = false, defaultValue = "default") String info) {
        log.info("starter hello");
        return "starter " + info;
    }
}
