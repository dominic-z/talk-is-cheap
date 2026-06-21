package org.talk.is.cheap.hello.spring.cloud.loadbalancer.frontend.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.talk.is.cheap.hello.spring.cloud.loadbalacner.backend.controller.HelloController;
import org.talk.is.cheap.hello.spring.cloud.loadbalacner.backend.controller.HiController;

@FeignClient(name = "backend-service", path = "/backend")
// 特定客户端配置
public interface HelloClient extends HelloController {

}
