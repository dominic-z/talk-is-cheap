package org.talk.is.cheap.hello.spring.openfeign.frontend.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.talk.is.cheap.hello.spring.cloud.message.GenericData;
import org.talk.is.cheap.hello.spring.openfeign.frontend.client.AsyncHijClient;
import org.talk.is.cheap.hello.spring.openfeign.frontend.client.HijClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequestMapping(path = "/frontend-service")
@RestController
@Slf4j
public class HijController {

    @Autowired
    private HijClient hijClient;

    @Autowired
    private AsyncHijClient asyncHijClient;

    @RequestMapping(path = "/hij", method = RequestMethod.POST)
    @ResponseBody
    public GenericData<String> hij(@RequestBody GenericData<String> reqBody) {

        log.info("frontend {}", reqBody.getData());
        GenericData<String> hij = hijClient.hij(reqBody);
        return GenericData.<String>builder()
                .code(hij.getCode())
                .data("frontend " + hij.getData())
                .msg("frontend " + hij.getMsg())
                .build();
    }


    @RequestMapping(path = "/slow-hij", method = RequestMethod.POST)
    @ResponseBody
    public GenericData<String> slowHij(@RequestBody GenericData<String> reqBody) {

        log.info("frontend slow {}", reqBody.getData());
        GenericData<String> hij = hijClient.slowHij(reqBody);
        return GenericData.<String>builder()
                .code(hij.getCode())
                .data("frontend slow" + hij.getData())
                .msg("frontend slow" + hij.getMsg())
                .build();
    }

    /**
     * 没测通
     * @param reqBody
     * @return
     */
    @RequestMapping(path = "/async-hij", method = RequestMethod.POST)
    @ResponseBody
    public GenericData<String> asyncHij(@RequestBody GenericData<String> reqBody) {

        log.info("frontend {}", reqBody.getData());
        CompletableFuture<GenericData<String>> hijFuture = asyncHijClient.asyncHij(reqBody);
        GenericData<String> frontendInFuture = null;
        try {

            frontendInFuture = hijFuture
                    .thenApply(hij -> {
                        log.info("frontend in future");
                        return GenericData.<String>builder()
                                .code(hij.getCode())
                                .data("frontend " + hij.getData())
                                .msg("frontend " + hij.getMsg())
                                .build();
                    })
                    .get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return frontendInFuture;

    }
}
