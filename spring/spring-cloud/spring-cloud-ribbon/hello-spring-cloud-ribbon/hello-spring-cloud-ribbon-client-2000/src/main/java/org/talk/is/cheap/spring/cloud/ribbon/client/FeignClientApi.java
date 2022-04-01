package org.talk.is.cheap.spring.cloud.ribbon.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author dominiczhu
 * @date 2022/3/31 1:15 下午
 */
@FeignClient("server")
public interface FeignClientApi {

    @GetMapping("/backend/echo")
    String echo(@RequestParam("value") String value);

}
