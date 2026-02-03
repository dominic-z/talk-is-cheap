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
    public Mono<User> getById(@PathVariable Integer id) {
        return userService.getById(id);
    }

    // 查询多个
    @GetMapping("/all")
    @ResponseBody
    public Flux<User> getAll() {
        return userService.getAll();
    }

    // 保存
    // 你不需要也不应该显式声明参数为 Mono<T>，Spring 会自动处理：
    // https://www.qianwen.com/share/chat/98606d93b1714cc7885f137c24661cca
    // 如果希望从mono里取出值，该怎么写 https://www.qianwen.com/share/chat/f6198471260a439ea132cea5ec0616b2
    //    正确写法（返回 Mono，让 Spring 处理）
    @PostMapping("/save")
    public Mono<Boolean> save(@RequestBody User user) {
        Mono<Boolean> saved = userService.save(user);
        return saved;
    }


    @Token
    @GetMapping("/testFilter")
    public Mono<User> testFilter(@RequestParam("id") String id, @RequestParam("name") String name) {
        return Mono.just(new User(id, name));
    }


    @GetMapping("/testExchange")
    public Mono<User> testExchange(ServerWebExchange exchange, @RequestParam("id") String id, @RequestParam("name") String name) {

        System.out.println(exchange.getLogPrefix());

        log.info("req: {}", exchange.getRequest());
        log.info("resp: {}", exchange.getResponse());
        log.info("session: {}", exchange.getSession());
        log.info("ac: {}", exchange.getApplicationContext());
        return Mono.just(new User(id, name));
    }


    @GetMapping("/error")
    public Mono<Void> error() {
        int i = 0;
        if (i == 0) {
            throw new RuntimeException("测试异常");
        }
        return Mono.empty();
    }


}

