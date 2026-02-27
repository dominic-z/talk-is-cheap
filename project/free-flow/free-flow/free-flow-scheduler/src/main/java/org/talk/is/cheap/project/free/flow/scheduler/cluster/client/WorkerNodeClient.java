package org.talk.is.cheap.project.free.flow.scheduler.cluster.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.router.URIs;

import java.net.URI;


@FeignClient(name = "worker-cluster-client", url = "None")
public interface WorkerNodeClient {

    @GetMapping(path = URIs.WorkerNodeURIs.PING)
    @ResponseBody
    HttpBody<String> ping(URI host);

    @GetMapping(path = URIs.WorkerNodeURIs.ALLOW_TO_RUN)
    @ResponseBody
    HttpBody<String> allowToRun(URI host, @RequestParam("zKPath") String zkPath, @RequestParam("zkData") String zkData);

    @GetMapping(path = URIs.WorkerNodeURIs.TERMINATE)
    @ResponseBody
    HttpBody<String> terminate(URI host);
}
