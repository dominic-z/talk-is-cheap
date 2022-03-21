package com.example.springboot.hellospringboot;

import com.example.springboot.hellospringboot.domain.pojo.Customers;
import com.example.springboot.hellospringboot.service.RedisAopLimitService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author dominiczhu
 * @version 1.0
 * @title RedisTest
 * @date 2022/1/14 8:51 下午
 */

@SpringBootTest(classes = HelloSpringBootApplication.class)
//@RunWith(SpringRunner.class) // 没搞清楚这东西有啥用
@Slf4j(topic = "root")
public class RedisTest {

    // 虽然叫StringTemplelate，含义是k和value类型都是string，其中value类型为String指的是，list、set等容器里的对象是value
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedisTemplate() {
        final ListOperations<String, String> ops = stringRedisTemplate.opsForList();
        final String listKey = "myList";
        stringRedisTemplate.delete(listKey);
        ops.rightPush(listKey, "1");
        ops.rightPush(listKey, "2");
        ops.rightPush(listKey, "3");
        ops.rightPush(listKey, "一只小猪");
        final List<String> myList = ops.range(listKey, 0, -1);
        log.info("myList:{}", myList);
    }

    @Test
    public void testSetNx() {

        String uuid = UUID.randomUUID().toString();
        log.info(uuid);

        final ValueOperations<String, String> stringOps = stringRedisTemplate.opsForValue();
        final Boolean lock = stringOps.setIfAbsent("lock", uuid, 10, TimeUnit.SECONDS);
        final Boolean lockAgain = stringOps.setIfAbsent("lock", uuid, 10, TimeUnit.SECONDS);
        log.info("lock: {}", lock);
        log.info("lockAgain: {}", lockAgain);

        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 " +
                "end";
        final Long unlock = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList("lock"), uuid);

        log.info("unlock: {}", unlock);
    }


    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testRedissonGetString() {
        final RBucket<String> bucket = redissonClient.getBucket("redisson-bucket");
        if (bucket.isExists()) {
            log.info("redisson-bucket res {}", bucket.get());
        } else {
            log.info("redisson-bucket not found");
            bucket.set("redisson-bucket");
        }

        final RBucket<String> bucketTryAgain = redissonClient.getBucket("redisson-bucket");
        if (bucketTryAgain.isExists()) {
            log.info("bucketTryAgain {}", bucketTryAgain.get());
        } else {
            log.info("still not found redisson-bucket");
        }
    }

    @Test
    public void testRedissonLock() throws Exception {

//        模拟A获取锁，A锁超时释放锁，B获取锁，A苏醒过来后，尝试再次解锁的故事，看看A会不会把B锁给释放了
//        结论，A不会再把B锁给释放了，看起来redisson帮我们做了类似于生成uuid的事情，查看源码好像是用ThreadId来代替uuid的
//        127.0.0.1:6379> hgetall redisson:lock
//          1) "7960afa4-2ab3-494d-ac63-55f4f44b39e5:58"
//          2) "1"
        final String lockKey = "redisson:lock";

        Runnable runnable = () -> {
            log.info("{} id {}", Thread.currentThread(), Thread.currentThread().getId());
            final RLock lock = redissonClient.getLock(lockKey);
            log.info("{} lock", Thread.currentThread());
            lock.lock(3, TimeUnit.SECONDS);
            log.info("{} before sleep, lock status: {}, isHeldByCurrentThread: {}",
                    Thread.currentThread(), lock.isLocked(), lock.isHeldByCurrentThread());

            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("{} after sleep, lock status: {}, isHeldByCurrentThread: {}",
                    Thread.currentThread(), lock.isLocked(), lock.isHeldByCurrentThread());
            lock.unlock();
        };

        Thread thread1 = new Thread(runnable);

        Thread thread2 = new Thread(runnable);

        thread1.start();
        Thread.sleep(3000);
        thread2.start();

        thread2.join();

        // 即使不舍过期时间，也会通过看门狗机制来控制锁不超时
        final RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
    }

    @Test
    public void testRedissonSemaphore() throws Exception {
        final String semaphoreName = "redisson:semaphore";

        final RBucket<Long> bucket = redissonClient.getBucket(semaphoreName);
        bucket.set(4L);

        Runnable runnable = () -> {
            final RSemaphore semaphore = redissonClient.getSemaphore(semaphoreName);
            try {
                log.info("{} acquire", Thread.currentThread());
                semaphore.acquire(2);
                log.info("{} acquired", Thread.currentThread());
                Thread.sleep(3000);
                log.info("{} release", Thread.currentThread());
                semaphore.release(2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);

        thread1.start();
        thread2.start();
        Thread.sleep(10);


        final RSemaphore semaphore = redissonClient.getSemaphore(semaphoreName);
        log.info("{} acquire", Thread.currentThread());
        semaphore.acquire(2);
        log.info("{} acquired", Thread.currentThread());
    }


    @Autowired
    RedisAopLimitService redisAopLimitService;

    @Test
    public void testRedisAopLimit() {

        for (int i = 0; i < 5; i++) {
            final String s = redisAopLimitService.callWithLimit(i);
            log.info(s);
        }
    }

    @Autowired
    @Qualifier("redisConnectionFactory")
    private RedisConnectionFactory redisConnectionFactory;
    @Test
    public void testSerializer() {

        // 第一托代码 默认序列化器
        RedisTemplate<String, Long> longDefaultSerializerTemplate = new RedisTemplate<>();
        longDefaultSerializerTemplate.setConnectionFactory(redisConnectionFactory);
        longDefaultSerializerTemplate.afterPropertiesSet();
        final ValueOperations<String, Long> longDefaultSerializerOps = longDefaultSerializerTemplate.opsForValue();
        final String longDefault = "long-default";
        longDefaultSerializerOps.set(longDefault, 1L);
        log.info("{} {}", longDefault, longDefaultSerializerOps.get(longDefault));

        String testLua = "return tonumber(ARGV[1]);";
        RedisScript<Long> defaultRedisScript = new DefaultRedisScript<>(testLua, Long.class);
        final Long res = longDefaultSerializerTemplate.execute(defaultRedisScript, Arrays.asList("s"), 1);
        log.info("{}", res);

        // 第二托代码 使用String序列化器
        RedisTemplate<String, Long> longStringSerializerTemplate = new RedisTemplate<>();
        longStringSerializerTemplate.setConnectionFactory(redisConnectionFactory);
        longStringSerializerTemplate.setKeySerializer(new StringRedisSerializer());
        longStringSerializerTemplate.setValueSerializer(new StringRedisSerializer());
        longStringSerializerTemplate.afterPropertiesSet();
        final ValueOperations<String, Long> longStringSerializerOps = longStringSerializerTemplate.opsForValue();
        try {
            final String longStringKey = "long-string";
            longStringSerializerOps.set(longStringKey, 1L);
            log.info("{} {}", longStringKey, longStringSerializerOps.get(longStringKey));
        } catch (Exception e) {
            log.error("Long cannot be encode or decode by StringRedisSerializer", e);
        }

        // 第三托代码 使用StringRedisSerializer做key的序列化器，因为key一般都是string
        // 使用Jackson做value的序列化器
        RedisTemplate<String, Long> longGenericJacksonTemplate = new RedisTemplate<>();
        longGenericJacksonTemplate.setConnectionFactory(redisConnectionFactory);
        longGenericJacksonTemplate.setKeySerializer(new StringRedisSerializer());
        longGenericJacksonTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        longGenericJacksonTemplate.afterPropertiesSet();
        final ValueOperations<String, Long> longGenericJacksonOps = longGenericJacksonTemplate.opsForValue();
        final String longJacksonKey = "long-jackson";
        longGenericJacksonOps.set(longJacksonKey, 1L);
        log.info("{} {}", longJacksonKey, longStringSerializerOps.get(longJacksonKey));

        // 使用StringRedisSerializer做key的序列化器，因为key一般都是string
        // 使用Jackson做value的序列化器
        RedisTemplate<String, DemoObj> objectGenericJacksonTemplate = new RedisTemplate<>();
        objectGenericJacksonTemplate.setConnectionFactory(redisConnectionFactory);
        objectGenericJacksonTemplate.setKeySerializer(new StringRedisSerializer());
        objectGenericJacksonTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        也可以指定类型
//        objectGenericJacksonTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(DemoObj.class));
        objectGenericJacksonTemplate.afterPropertiesSet();
        final ValueOperations<String, DemoObj> obgGenericJacksonOps = objectGenericJacksonTemplate.opsForValue();
        final String objJacksonKey = "obj-jackson";
        obgGenericJacksonOps.set(objJacksonKey, DemoObj.getDemoObj());
        log.info("{} {}", objJacksonKey, obgGenericJacksonOps.get(objJacksonKey));



    }

    @Data
    private static class DemoObj {
        private long id;
        private String name;
        private List<String> habits;

        static DemoObj getDemoObj() {
            final DemoObj demoObj = new DemoObj();
            demoObj.setId(20);
            demoObj.setName("demo");
            demoObj.setHabits(Arrays.asList("sing", "dance"));
            return demoObj;
        }
    }
}
