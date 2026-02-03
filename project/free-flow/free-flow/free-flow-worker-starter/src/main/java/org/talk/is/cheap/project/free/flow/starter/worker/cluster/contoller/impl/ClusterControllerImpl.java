package org.talk.is.cheap.project.free.flow.starter.worker.cluster.contoller.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.starter.worker.cluster.contoller.ClusterController;
import org.talk.is.cheap.project.free.flow.starter.worker.cluster.service.ClusterService;


@RequestMapping
@RestController
@Slf4j
public class ClusterControllerImpl implements ClusterController {

    @Autowired
    private ClusterService clusterService;

    private int i = 0;

    @Override
    public HttpBody<String> ping() {
//        i++;
//        log.info("i: {}", i);
//        if (i > 5) {
//            throw new RuntimeException("eee");
//        }
        return HttpBody.<String>builder().data("pong").code(ResultCode.SUCCESS.getCode()).build();
    }

    /**
     * scheduler允许该worker进入runnable的状态
     *
     * @return
     */
    @Override
    public HttpBody<String> allowToRun(String zkPath, String zkData) {
        HttpBody<String> resp = new HttpBody<>();

        try {

            clusterService.becomeRunnable();
            resp.success("");
        } catch (Exception e) {
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;
    }

    @Override
    public HttpBody<String> terminate() {
        HttpBody.HttpBodyBuilder<String> builder = HttpBody.<String>builder();
        try {
            clusterService.terminate();
            return builder.code(ResultCode.SUCCESS.getCode()).build();
        } catch (Exception e) {
            return builder.code(ResultCode.FAIL.getCode()).msg(e.getMessage()).build();
        }
    }
}
