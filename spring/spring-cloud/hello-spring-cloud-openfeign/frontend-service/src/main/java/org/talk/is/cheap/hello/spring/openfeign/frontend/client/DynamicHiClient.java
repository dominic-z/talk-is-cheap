package org.talk.is.cheap.hello.spring.openfeign.frontend.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.talk.is.cheap.hello.spring.openfeign.common.message.GenericData;

import java.net.URI;
import java.util.Map;


/**
 * 一个动态的Feign客户端，其实就是对各种http请求做了包装，包装成RPC的形式供外界调用。
 */
@FeignClient(value = "dynamicFeignClient", url = "None")
public interface DynamicFeignHttpClient {

    /**
     * 统一回调接口方法，请求消息体格式为JSON，响应消息体格式也为JSON
     *
     * @param host     接口主机地址，如：http://localhost:8080
     * @param path     接口路径，如：/test/hello
     * @param body     请求消息体对象
     * @return
     */
    @RequestMapping(path = "{path}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    GenericData<String> post(URI host,
                             @PathVariable("path") String path,
                             @RequestBody Object body);

    /**
     * 统一回调接口方法，请求消息体格式为JSON，响应消息体格式也为JSON
     *
     * @param uri      完整的请求路径地址，如：http://localhost:8080/test/hello
     * @param body     请求消息体对象
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    GenericData<String> post(URI uri,
                             @RequestBody Object body);


    /**
     * @param uri      完整的请求路径地址，如：http://localhost:8080/test/hello
     * @param path     接口路径，如：/test/hello
     * @param queryMap 动态URL参数集合
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    GenericData<String> get(URI uri, @SpringQueryMap Map<String, Object> queryMap);
}
