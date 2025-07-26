package org.talk.is.cheap.project.free.flow.scheduler.cluster.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.message.HttpBody;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.SchedulerClusterManager;

@RestController
@RequestMapping(path = "/cluster/scheduler")
public class ClusterController {

    @Autowired
    private SchedulerClusterManager schedulerClusterManager;

    @RequestMapping(path = "/ping", method = RequestMethod.GET)
    @ResponseBody
    public HttpBody<String> ping() {
        return HttpBody.<String>builder().msg(schedulerClusterManager.getSchedulerId() + ":pong").code(0).build();
    }
}
