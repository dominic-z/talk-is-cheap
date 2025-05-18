package org.example.bff.client;


import org.example.backend.controller.BackendController;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RestController;


// path注解是一个前缀，因为FeignClient不能加RequestMapping注解，所以地址前缀卸载这个字段里面了
@FeignClient(value = "backend",path = "/backend")
public interface BackendClient extends BackendController {

}
