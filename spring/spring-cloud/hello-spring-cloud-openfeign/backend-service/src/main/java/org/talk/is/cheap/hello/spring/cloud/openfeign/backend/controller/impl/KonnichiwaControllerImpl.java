package org.talk.is.cheap.hello.spring.cloud.openfeign.backend.controller.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.hello.spring.cloud.message.GenericData;
import org.talk.is.cheap.hello.spring.cloud.openfeign.backend.controller.KonnichiwaController;
@Slf4j
@RestController
@RequestMapping(path = "/backend-service")
public class KonnichiwaControllerImpl implements KonnichiwaController {
    @Override
    public GenericData<String> konnichiwa(GenericData<String> reqBody) {
        log.info("backend konnichiwa： {}",reqBody.getData());
        return GenericData.<String>builder().code(0).data("backend konnichiwa "+reqBody.getData()).build();
    }
}
