package toolkits.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import toolkits.messages.DemoRequest;

/**
 * @author dominiczhu
 * @date 2021/5/18 下午8:37
 */
@FeignClient(name="HttpBinClient",url = "http://httpbin.org")
public interface HttpBinClient {
    @ResponseBody
    @RequestMapping(value = "/post",
            method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    String post(@RequestBody DemoRequest req);
}
