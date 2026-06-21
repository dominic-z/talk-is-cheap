package org.talk.is.cheap.hello.spring.openfeign.frontend.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.talk.is.cheap.hello.spring.openfeign.common.message.GenericData;

import java.net.URI;
import java.util.Map;


/**
 * 也是一个动态的客户端，但是专门针对某些路径的，是在DynamicFeignHttpClient看到openfeign关于动态url的描述，感觉这样也行
 * 所以试了试，结果真的可以
 */
@FeignClient(name = "dynamicHiClient", url = "None")
public interface DynamicHiClient {

    @RequestMapping(path = "/hi", method = RequestMethod.POST)
    GenericData<String> hi(URI host, @RequestBody GenericData<String> reqBody);

    @RequestMapping(path = "/get-hi", method = RequestMethod.GET)
    GenericData<String> getHi(URI host, @RequestParam(name = "data") String data, @RequestParam(name = "msg") String msg);
}
