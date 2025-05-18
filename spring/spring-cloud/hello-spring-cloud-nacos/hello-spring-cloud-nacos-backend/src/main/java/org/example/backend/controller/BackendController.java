package org.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*

 这个接口即用作当前模块实现controller，也用作其他模块的feignClient的实现
 但是因为feighClient的客户端不能带@RequestMapping注解，所以这个@RequestMapping被放在实现类上了
 如果这个接口带了@RequestMapping注解，那么继承这个接口的feignclient在生成对象的时候会抛出异常：
 FignClient不允许带有@RequestMapping注解
*/

public interface BackendController {

    @GetMapping(path = "/getHello")
    ResponseEntity<String> getHello(@RequestParam("myName") String name,@RequestParam("myId") int id);

}
