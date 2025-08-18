package org.talk.is.cheap.project.free.flow.scheduler.cluster.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.net.URI;


@FeignClient(name = "worker-cluster-client", url = "None")
public interface WorkerClusterClient {

    @GetMapping(path = "/worker/ping")
    @ResponseBody
    HttpBody<String> ping(URI host);

}
