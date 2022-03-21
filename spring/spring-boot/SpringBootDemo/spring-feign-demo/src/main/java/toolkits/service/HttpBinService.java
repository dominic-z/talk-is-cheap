package toolkits.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import toolkits.clients.HeaderedHttpBinClient;
import toolkits.clients.HttpBinClient;
import toolkits.clients.PostFormHttpBinClient;
import toolkits.messages.DemoRequest;
import toolkits.messages.DemoResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dominiczhu
 * @version 1.0
 * @title DemoService
 * @date 2021/5/18 下午8:34
 */
@RestController
@RequestMapping("/httpBin")
public class HttpBinService {

    @Autowired
    private HttpBinClient demoClient;

    @Autowired
    private HeaderedHttpBinClient configuredDemoClient;

    @Autowired
    private PostFormHttpBinClient postFormHttpBinClient;

    @RequestMapping(value = "/hello", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    public DemoResponse hello(@RequestBody DemoRequest req) {
        DemoResponse resp = new DemoResponse();
        System.out.println(req);
        resp.setContent(req.getContent());
        return resp;
    }

    @RequestMapping(value = "/post", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    public String postClient(@RequestBody DemoRequest req) {
        return demoClient.post(req);
    }

    @RequestMapping(value = "/headered_post", method = {RequestMethod.POST}, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public String configuredPostClient(@RequestBody DemoRequest req) {
        return configuredDemoClient.post(req);
    }

    @RequestMapping(value = "/post_form", method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    public String postForm(@RequestBody DemoRequest req) {
        Map<String, Object> form = new HashMap<>();
        form.put("content", req.getContent());
        return postFormHttpBinClient.post(req);
    }
}
