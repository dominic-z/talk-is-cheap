package com.example.redis_spring_client;

import com.example.redis_spring_client.pojo.User;
import com.example.redis_spring_client.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
class RedisSpringClientApplicationTests {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisCacheManager cacheManager;

    @Autowired
    private CacheService cacheService;

    @Test
    void contextLoads() {
    }

    @Test
    public void testKey() {
        String key = "runoobkey";
        // set runoobkey redis
        redisTemplate.opsForValue().set(key, "redis");
        // get runoobkey
        Object value = redisTemplate.opsForValue().get(key);
        log.info("value: {}", value);
        // del runoobkey
        Boolean delete = redisTemplate.delete(key);
        log.info("delete {}", delete);
        // exists runoobkey
        Boolean hasK = redisTemplate.hasKey(key);
        log.info("hasK {}", hasK);

        redisTemplate.opsForValue().set(key, "redis");

        redisTemplate.expire(key, 100, TimeUnit.MILLISECONDS);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Long expire = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        log.info("expire {}", expire);
    }


    @Test
    public void testCacheManager() {

        long id = 123L;
        // 清除缓存
        cacheService.deleteUser(id);
        User userById = cacheService.getUserById(id);

        // 在这打断点，然后去redis-cli里执行keys *，发现有了一个key叫做users::123
        Cache cache = cacheManager.getCache("users");
        Cache.ValueWrapper valueWrapper = cache.get(id);
        log.info("valueWrapper : {}", valueWrapper);

        // 可以看出走缓存了，因为这个方法里的log没有执行。
        cacheService.getUserById(id);

    }
}
