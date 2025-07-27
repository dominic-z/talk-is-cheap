package hello.spring.boot3.spi.starter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

public interface StarterController {
    @GetMapping(path = "/hello")
    @ResponseBody
    String starterHello(@RequestParam(name = "info",required = false, defaultValue = "default") String info);
}
