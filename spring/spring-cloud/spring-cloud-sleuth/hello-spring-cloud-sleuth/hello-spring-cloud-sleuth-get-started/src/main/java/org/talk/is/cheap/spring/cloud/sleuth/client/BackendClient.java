package org.talk.is.cheap.spring.cloud.sleuth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author dominiczhu
 * @version 1.0
 * @title BackendClient
 * @date 2022/3/29 2:01 下午
 */
@FeignClient("sleuth")
public interface BackendClient {

    @GetMapping("/backend/echo")
    String echo(@RequestParam("value") String value);
}
