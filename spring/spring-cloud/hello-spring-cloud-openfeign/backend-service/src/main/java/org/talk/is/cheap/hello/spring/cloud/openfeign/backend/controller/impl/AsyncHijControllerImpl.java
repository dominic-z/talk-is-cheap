package org.talk.is.cheap.hello.spring.cloud.openfeign.backend.controller.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.hello.spring.openfeign.common.message.GenericData;
import org.talk.is.cheap.hello.spring.cloud.openfeign.backend.controller.AsyncHijController;

import java.util.concurrent.CompletableFuture;

/**
 * RestController必须挂在实现类上，
 * RequestMapping倒是可以写在接口上，但是我认为这样写在具体实现类上更好一下
 * 这样的话，client类可以直接继承HijController拥有所有的方法，而无需再重写方法
 */
@Slf4j
@RestController
@RequestMapping(path = "/backend-service")
public class AsyncHijControllerImpl implements AsyncHijController {

    @Override
    public CompletableFuture<GenericData<String>> asyncHij(GenericData<String> req) {
        log.info("async backend hij： {}",req.getData());
        return CompletableFuture.completedFuture(GenericData.<String>builder().code(0).data("backend hij "+req.getData()).build());
    }
}
