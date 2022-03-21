package server.services;

import server.messages.DemoRequest;
import server.messages.DemoResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dominiczhu
 * @version 1.0
 * @title server.services.DemoService
 * @date 2021/5/18 下午8:34
 */
@RestController
@RequestMapping("/localServer")
public class DemoService {


    @RequestMapping(value = "/hello", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    public DemoResponse hello(@RequestBody DemoRequest req) throws InterruptedException {
        DemoResponse resp = new DemoResponse();
        System.out.println(req);
        Thread.sleep(70000);
        resp.setContent(String.format("resp: %s", req.getContent()));
        return resp;
    }

}
