package hello.spring.boot3.spi.starter.starter.feign.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 既然feign能够为不在当前project里的client创建对象
 * 那我想知道在starter里创建FeignClient能否通过EnableStarterFeignClientsConfig注入到实际应用的容器里，所以突发奇想做个测试
 * 需要借用hello openfeign里的backendservice
 *
 * 结论是不能。
 */
@FeignClient(
        name = "hiClient",
        url = "http://localhost:8080/backend-service"
)
public interface HiClient {

    @RequestMapping(path = "/hi",method = RequestMethod.POST)
    GenericData<String> hi(@RequestBody GenericData<String> reqBody);

}
