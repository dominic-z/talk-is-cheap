package org.talk.is.cheap.database.redis.config.client;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest(classes = Main.class)
@Slf4j
public class AppTest {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testRedis(){
        String key = "runoobkey";
        redisTemplate.delete(key);

        // set runoobkey redis
        redisTemplate.opsForValue().set(key, "redis");
        redisTemplate.opsForValue().setIfAbsent(key, "redis11");
        // get runoobkey
        Object value = redisTemplate.opsForValue().get(key);
        log.info("value: {}", value);
    }


    @Test
    public void testRedission(){
// ==================== 核心操作：字符串（Bucket） ====================
        // 1. 获取字符串操作对象（RBucket 对应 Redis 的 String 类型）
        RBucket<String> userBucket = redissonClient.getBucket("user:name");

        // 2. 存值（等价于 Redis 的 SET 命令）
        userBucket.set("张三");
        System.out.println("存入值：张三");

        // 3. 取值（等价于 Redis 的 GET 命令）
        String userName = userBucket.get();
        System.out.println("取出值：" + userName); // 输出：取出值：张三

        // 4. 判断键是否存在（等价于 Redis 的 EXISTS 命令）
        boolean exists = userBucket.isExists();
        System.out.println("键是否存在：" + exists); // 输出：true

        // 5. 删除键（等价于 Redis 的 DEL 命令）
        boolean deleted = userBucket.delete();
        System.out.println("删除结果：" + deleted); // 输出：true

        // 6. 再次取值（删除后为 null）
        String deletedName = userBucket.get();
        System.out.println("删除后取值：" + deletedName); // 输出：null
    }
}
