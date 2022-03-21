package toolkits.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import toolkits.clients.LocalServerClient;
import toolkits.messages.DemoRequest;
import toolkits.messages.DemoResponse;

/**
 * @author dominiczhu
 * @version 1.0
 * @title DemoService
 * @date 2021/5/18 下午8:34
 */
@RestController
@RequestMapping("/localServer")
public class LocalServerService {

    @Autowired
    private LocalServerClient localServerClient;

    @RequestMapping(value = "hello", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    public DemoResponse hello(@RequestBody DemoRequest req) {
        System.out.println(req);
        try {

            DemoResponse resp = localServerClient.hello(req);
            return resp;

        }catch (Exception e){
            e.printStackTrace();
            return new DemoResponse();
        }
    }

}
