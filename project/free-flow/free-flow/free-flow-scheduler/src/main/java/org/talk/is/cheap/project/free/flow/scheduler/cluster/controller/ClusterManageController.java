package org.talk.is.cheap.project.free.flow.scheduler.cluster.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.QueryClusterInfoReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.QueryClusterInfoResp;
import org.talk.is.cheap.project.free.flow.common.router.URIs;

/**
 * 面向前端的cluster
 */
public interface ClusterManageController {

    @RequestMapping(path = URIs.ClusterManageURIs.NODES, method = RequestMethod.POST)
    @ResponseBody
    QueryClusterInfoResp queryNodes(@RequestBody QueryClusterInfoReq req);

    /**
     * 停止一个worker，大体流程：
     * 1. 发出终止指令；
     * 2. worker进入终止中的状态；
     * 3. worker控制自己的每个任务进入到stw的状态，每当一个任务进入到stw的状态，就通知scheduler对任务重新指派
     * 4. 持续步骤3直到没有运行的任务了
     * 5. worker上报scheduler自己可以被安全终止
     * @param workerAddress
     * @return
     */
    @RequestMapping(path = URIs.ClusterManageURIs.TERMINATE_WORKER, method = RequestMethod.POST)
    @ResponseBody
    HttpBody<String> tryTerminateWorker(@RequestParam("workerAddress") String workerAddress);


}
