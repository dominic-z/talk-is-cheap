package toolkits.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import toolkits.config.InterceptorDemo;
import toolkits.messages.DemoRequest;

/**
 * @author dominiczhu
 * @date 2021/5/18 下午8:37
 */
@FeignClient(name = "HeaderedHttpBinClient",url = "http://httpbin.org", configuration = {InterceptorDemo.class, FeignClientsConfiguration.class})
public interface HeaderedHttpBinClient {
    @ResponseBody
    @RequestMapping(value = "/post",
            method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
    String post(@RequestBody DemoRequest req);
}
