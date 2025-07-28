package org.talk.is.cheap.hello.spring.openfeign.frontend.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.hello.spring.openfeign.common.message.GenericData;

import java.util.concurrent.CompletableFuture;

/**
 * 失败了，没有测通feign的异步调用，算了算了，可以手动写一个线程池做异步调用把。
 */
@FeignClient(
        name = "asyncHijClient",
        url = "http://localhost:8080/backend-service"
//        fallback = AsyncHijClientFallback.class
)
public interface AsyncHijClient  {
    @RequestMapping(path = "/async-hij", method = RequestMethod.POST)
    @ResponseBody
    CompletableFuture<GenericData<String>> asyncHij(@RequestBody GenericData<String> req);
}
