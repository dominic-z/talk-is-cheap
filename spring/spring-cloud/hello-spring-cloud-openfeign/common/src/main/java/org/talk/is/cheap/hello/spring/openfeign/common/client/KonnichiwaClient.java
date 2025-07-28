package org.talk.is.cheap.hello.spring.openfeign.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.talk.is.cheap.hello.spring.openfeign.common.message.GenericData;

@FeignClient(
        name = "konnichiwaClient",
        url = "http://localhost:8080/backend-service"
)
public interface KonnichiwaClient {

    @RequestMapping(path = "/konnichiwa",method = RequestMethod.POST)
    GenericData<String> konnichiwa(@RequestBody GenericData<String> reqBody);
}
