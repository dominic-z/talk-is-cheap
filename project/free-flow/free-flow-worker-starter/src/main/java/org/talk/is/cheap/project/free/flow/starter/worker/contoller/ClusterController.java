package org.talk.is.cheap.project.free.flow.starter.worker.contoller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;


public interface ClusterController {

    @GetMapping(path = "/ping")
    @ResponseBody
    HttpBody<String> ping();
}
