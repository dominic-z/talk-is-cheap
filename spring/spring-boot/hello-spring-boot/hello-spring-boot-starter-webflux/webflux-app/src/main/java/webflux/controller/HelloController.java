package webflux.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import webflux.anno.Token;
import webflux.message.pojo.User;
import webflux.service.UserService;

@RestController
@RequestMapping("/flux")
@Slf4j
public class HelloController {

    @Autowired
    private UserService userService;


    // 根据id查询
    @GetMapping("/{id}")
    public Mono<User> getById(@PathVariable Integer id){
        return userService.getById(id);
    }

    // 查询多个
    @GetMapping("/all")
    @ResponseBody
    public Flux<User> getAll(){
        return userService.getAll();
    }

    // 保存
    @PostMapping("/save")
    public Mono<ResponseEntity<String>> save(@RequestBody Mono<User> userMono){
        Mono<Void> save = userService.save(userMono);

        ResponseEntity<String> resp = new ResponseEntity<>("success", HttpStatusCode.valueOf(200));
        return Mono.just(resp);
    }


    @Token
    @GetMapping("/testFilter")
    public Mono<User> testFilter(@RequestParam("id") String id,@RequestParam("name") String name){
        return Mono.just(new User(id, name));
    }


    @GetMapping("/testExchange")
    public Mono<User> testExchange(ServerWebExchange exchange,@RequestParam("id") String id,@RequestParam("name") String name){

        System.out.println(exchange.getLogPrefix());

        log.info("req: {}",exchange.getRequest());
        log.info("resp: {}",exchange.getResponse());
        log.info("session: {}",exchange.getSession());
        log.info("ac: {}",exchange.getApplicationContext());
        return Mono.just(new User(id, name));
    }


    @GetMapping("/error")
    public Mono<Void> error(){
        int i=0;
        if(i==0){
            throw new RuntimeException("测试异常");
        }
        return Mono.empty();
    }


}

