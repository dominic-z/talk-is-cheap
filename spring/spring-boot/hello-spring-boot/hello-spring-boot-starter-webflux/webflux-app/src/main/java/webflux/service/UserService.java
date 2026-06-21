package webflux.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import webflux.message.pojo.User;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class UserService {

    // 模拟数据库存储
    private Map<Integer, User> map = new HashMap<>();

    private AtomicInteger counter = new AtomicInteger(0);
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());

    public UserService() {
        map.put(1, new User("1", "zhangsan"));
        map.put(2, new User("2", "lisi"));
        map.put(3, new User("3", "wangwu"));
    }

    // 根据id查询
    public Mono<User> getById(Integer id) {
        // 返回数据或空值
        return Mono.justOrEmpty(map.get(id));
    }

    // 查询多个
    public Flux<User> getAll() {
        return Flux.fromIterable(map.values());
    }

    // 保存
    public Mono<Boolean> saveMono(Mono<User> userMono) {

        return userMono.map(user -> {
            log.info("新增user：{}", user);
            if (counter.incrementAndGet() % 2 == 0) {
                throw new RuntimeException("id是偶数");
            }
            int id = map.size() + 1;
            map.put(id, user);
            return true;
        }).onErrorResume(e->{
            log.error("出错了，执行一些fallback逻辑",e);
            return Mono.just(false);
        }); // 最后置空
    }

    public Mono<Boolean> save(User user) {
        Mono<Boolean> mono = Mono.fromCallable(() -> {
                    log.info("新增user：{}", user);
                    if (counter.incrementAndGet() % 2 == 0) {
                        throw new RuntimeException("id是偶数");
                    }
                    int id = map.size() + 1;
                    map.put(id, user);
                    return true;
                })
                .onErrorResume(e -> {
                    log.error("出错了，执行一些fallback逻辑", e);
                    return Mono.just(false);
                })
                .subscribeOn(Schedulers.fromExecutor(THREAD_POOL_EXECUTOR));
        return mono;
    }
}


