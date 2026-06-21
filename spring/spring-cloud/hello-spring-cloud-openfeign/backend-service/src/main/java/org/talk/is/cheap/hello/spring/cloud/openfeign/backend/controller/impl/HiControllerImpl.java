package org.talk.is.cheap.hello.spring.cloud.openfeign.backend.controller.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.hello.spring.openfeign.common.message.GenericData;
import org.talk.is.cheap.hello.spring.cloud.openfeign.backend.controller.HiController;


@Slf4j
@RestController
@RequestMapping(path = "/backend-service")
public class HiControllerImpl implements HiController {

    @Override
    public GenericData<String> hi(GenericData<String> reqBody) {
        log.info("backend hi： {}", reqBody.getData());
        return GenericData.<String>builder().code(0).data("backend hi " + reqBody.getData()).build();
    }

    @Override
    public GenericData<String> getHi(String data, String msg) {
        log.info("backend get hi： {}", data);
        return GenericData.<String>builder().code(0).data("backend get hi " + data).msg("backend get hi " + msg).build();
    }
}
