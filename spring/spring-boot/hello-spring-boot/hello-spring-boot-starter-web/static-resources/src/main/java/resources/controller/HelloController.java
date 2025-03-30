package resources.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class HelloController {


    @ResponseBody
    @RequestMapping("/postHello")
    public ResponseEntity<Map<String,String>> postHello(RequestEntity<Map<String,String>> request){
        log.info("request {}",request.getBody());
        Map<String, String> body = request.getBody();
        ResponseEntity<Map<String, String>> responseEntity = new ResponseEntity<Map<String, String>>(body,HttpStatus.OK);
        return responseEntity;
    }
}
