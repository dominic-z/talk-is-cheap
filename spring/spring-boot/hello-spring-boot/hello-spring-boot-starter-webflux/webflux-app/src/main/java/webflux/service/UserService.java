package webflux.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import webflux.message.pojo.User;


import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    // 模拟数据库存储
    private Map<Integer, User> map = new HashMap<>();

    public UserService() {
        map.put(1, new User("1","zhangsan"));
        map.put(2, new User("2","lisi"));
        map.put(3, new User("3","wangwu"));
    }

    // 根据id查询
    public Mono<User> getById(Integer id){
        // 返回数据或空值
        return Mono.justOrEmpty(map.get(id));
    }

    // 查询多个
    public Flux<User> getAll(){
        return Flux.fromIterable(map.values());
    }

    // 保存
    public Mono<Void> save(Mono<User> userMono){
        return userMono.doOnNext(user -> {
            int id = map.size() + 1;
            map.put(id, user);
        }).thenEmpty(Mono.empty()); // 最后置空
    }
}


