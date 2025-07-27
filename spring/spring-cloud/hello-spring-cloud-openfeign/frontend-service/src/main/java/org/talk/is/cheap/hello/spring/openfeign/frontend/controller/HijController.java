package org.talk.is.cheap.hello.spring.openfeign.frontend.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.talk.is.cheap.hello.spring.cloud.message.GenericData;
import org.talk.is.cheap.hello.spring.openfeign.frontend.client.AsyncHijClient;
import org.talk.is.cheap.hello.spring.openfeign.frontend.client.DynamicFeignHttpClient;
import org.talk.is.cheap.hello.spring.openfeign.frontend.client.HijClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private DynamicFeignHttpClient dynamicFeignHttpClient;

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
     *
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


    @RequestMapping(path = "/dynamic-hello", method = RequestMethod.POST)
    @ResponseBody
    public GenericData<List<String>> dynamicHello(@RequestBody GenericData<List<String>> reqBody) {
        ArrayList<String> strings = new ArrayList<>();
        URI absoluteURI = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/backend-service/hi").build().toUri();
        URI hostURI = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/").build().toUri();
        String path = "/backend-service/konnichiwa";

        for (int i = 0; i < reqBody.getData().size(); i++) {
            String s = reqBody.getData().get(i);
            GenericData<String> resp = null;
            if (i % 2 == 0) {
                resp = dynamicFeignHttpClient.post(absoluteURI, GenericData.<String>builder().data(s).build());
            } else {
                resp = dynamicFeignHttpClient.post(hostURI, path, GenericData.<String>builder().data(s).build());
            }

            strings.add("frontend-" + resp.getData());
        }

        return GenericData.<List<String>>builder().data(strings).code(0).build();

    }


    @RequestMapping(path = "/dynamic-get-hello", method = RequestMethod.GET)
    @ResponseBody
    public GenericData<String> dynamicGetHello(@RequestParam(name = "data") String data, @RequestParam(name = "msg") String msg) {
        URI absoluteURI = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/backend-service/get-hi").build().toUri();

        GenericData<String> resp = dynamicFeignHttpClient.get(absoluteURI, Map.of("data", data, "msg", msg));
        return GenericData.<String>builder().data("frontend-" + resp.getData()).msg("frontend-" + resp.getMsg()).code(0).build();

    }
}
