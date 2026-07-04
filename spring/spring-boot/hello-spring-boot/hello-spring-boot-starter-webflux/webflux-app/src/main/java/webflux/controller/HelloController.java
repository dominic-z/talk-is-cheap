package webflux.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import webflux.anno.Token;
import webflux.message.pojo.User;
import webflux.service.UserService;

import java.util.List;

// 当你使用 @RestController 时：@ResponseBody 完全多余。因为 @RestController = @Controller + @ResponseBody，它已经默认对类中所有方法生效。
// https://www.qianwen.com/share/chat/353c18c527a545ca8fd01d90aaddb732
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
    public Flux<User> getAll() {
        return userService.getAllMono();
    }


    // webflux操作response，
    // 对于请求体，千问又说建议将请求体包装为Mono对象了。。。随便吧
    @PostMapping("/response/save")
    public Mono<ResponseEntity<List<User>>> getById(@RequestBody Mono<User> userMono,
                                                    // ✅ 请求头：用 @RequestHeader 获取指定头，或 HttpHeaders 获取全部
                                                    @RequestHeader(value = "X-Request-Id", required = false) String requestId,
                                                    @RequestHeader(value = "X-Optional", required = false) String optionalHeader                                                    ) {

        log.info("requestId: {}, optionalHeader: {}", requestId, optionalHeader);
        Mono<Boolean> saved = userService.saveMono(userMono);

        return saved.map(s->
                 ResponseEntity.status(HttpStatus.OK)
                        .header("X-Custom-Response", "value-" + requestId)
                        .header("X-Processed-At", String.valueOf(System.currentTimeMillis()))
                        .header("res", String.valueOf(s))
                        .body(userService.getAll())
        );
    }

    // 保存
    // 你不需要也不应该显式声明入参参数为 Mono<T>，Spring 会自动处理：
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


    // 可以获得完整的请求对象
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

