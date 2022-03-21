package toolkits.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
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
@FeignClient(name = "PostFormHttpBinClient",url = "http://httpbin.org", configuration = {FeignClientsConfiguration.class})
public interface PostFormHttpBinClient {
    @ResponseBody
    @RequestMapping(value = "/post",
            consumes= {MediaType.MULTIPART_FORM_DATA_VALUE},
            method = {RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_VALUE)
//    String post(Map<String, ?> formParams);
    String post(@RequestBody DemoRequest req);

}
