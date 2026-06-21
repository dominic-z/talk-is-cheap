package resources.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resources.message.EnumsReq;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class HelloController {

    @ResponseBody
    @RequestMapping(value = "/getHello", method = RequestMethod.GET)
    public ResponseEntity<String> getHello(@RequestParam("param") String param) {
        log.info("param {}", param);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(param, HttpStatus.OK);
        return responseEntity;
    }

    /**
     * 如果使用requestEntity，不需要加@RequestBody，
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/postHelloJson", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, String>> postHelloJson(RequestEntity<Map<String, String>> request) {
        log.info("request {}", request.getBody());
        Map<String, String> body = request.getBody();
        ResponseEntity<Map<String, String>> responseEntity = new ResponseEntity<Map<String, String>>(body, HttpStatus.OK);
        return responseEntity;
    }


    @ResponseBody
    @RequestMapping(value = "/postHelloText", method = RequestMethod.POST, consumes = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<Object> postHelloText(@RequestBody String reqBody) {
        log.info("request {}", reqBody);
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(reqBody, HttpStatus.OK);
        return responseEntity;
    }


    //    通过postman添加发送请求
//<person>
//    <name>John Doe</name>
//    <age>30</age>
//</person>
    @ResponseBody
    @RequestMapping(value = "/postHelloXml", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Object> postHelloXml(@RequestBody Object reqBody) {
        log.info("request {}", reqBody);
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(reqBody, HttpStatus.OK);
        return responseEntity;
    }


    @RequestMapping(value = "/convert-enum", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EnumsReq convertEnum(@RequestBody EnumsReq req) {
        log.info("EnumsReq :{}", req);
        return req;
    }
    @RequestMapping(value = "/timeConsumingJob", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> timeConsumingJob(@RequestParam("duration") int duration ){

        try {
            Thread.sleep(duration* 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ResponseEntity<String> responseEntity = new ResponseEntity<>(duration+"", HttpStatus.OK);
        return responseEntity;
    }
}
