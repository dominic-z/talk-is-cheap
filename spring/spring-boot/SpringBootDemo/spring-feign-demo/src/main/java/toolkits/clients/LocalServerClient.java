package toolkits.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import toolkits.messages.DemoRequest;
import toolkits.messages.DemoResponse;

/**
 * @author dominiczhu
 * @date 2021/5/18 下午8:37
 */
@FeignClient(name = "LocalServerClient", url = "http://localhost:8081/localServer", contextId = "LocalServerClient")
public interface LocalServerClient {
    @ResponseBody
    @RequestMapping(value = "/hello",
            method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    DemoResponse hello(@RequestBody DemoRequest req);
}
