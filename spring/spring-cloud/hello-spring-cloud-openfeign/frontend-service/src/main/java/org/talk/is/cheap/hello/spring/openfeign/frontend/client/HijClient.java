package org.talk.is.cheap.hello.spring.openfeign.frontend.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.talk.is.cheap.hello.spring.cloud.openfeign.backend.controller.HijController;
import org.talk.is.cheap.hello.spring.openfeign.frontend.client.config.HijClientConfiguration;
import org.talk.is.cheap.hello.spring.openfeign.frontend.client.interceptor.HijClientInterceptor;

@FeignClient(
        name = "hijClient",
        url = "http://localhost:8080/backend-service",
        configuration = {HijClientConfiguration.class, HijClientInterceptor.class},
        fallback = HijClientFallback.class
)
public interface HijClient extends HijController {
//    @RequestMapping(path = "/hij",method = RequestMethod.POST)
//    @ResponseBody
//    GenericData<String> hij(@RequestBody GenericData<String> req);
}
