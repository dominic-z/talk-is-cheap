package com.example.redis_spring_client;

import com.example.redis_spring_client.pojo.User;
import com.example.redis_spring_client.service.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
@Slf4j
class RedisSpringClientApplicationTests {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisCacheManager cacheManager;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private CacheService cacheService;

    @Test
    void contextLoads() {
    }


    @Test
    public void testRedisString() {
        String key = "runoobkey";
        redisTemplate.delete(key);

        // set runoobkey redis
        redisTemplate.opsForValue().set(key, "redis");
        redisTemplate.opsForValue().setIfAbsent(key, "redis11");
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
//        设置过期时间，毫秒，pexpire runoobkey 100
        redisTemplate.expire(key, 100, TimeUnit.MILLISECONDS);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        获取过期时间，ttl runoobkey
        Long expire = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        log.info("expire {}", expire);


//        persist，持久化 persist key
        redisTemplate.opsForValue().set(key, "redis", 100L, TimeUnit.SECONDS);
        log.info(" expire {}", redisTemplate.getExpire(key, TimeUnit.MILLISECONDS));
        redisTemplate.opsForValue().getAndPersist(key);
//        这种操作也可以，boundValueOps(key)会返回一个专门操作这个key的op
//        redisTemplate.boundValueOps(key).persist();
        log.info("expire after persist {}", redisTemplate.getExpire(key, TimeUnit.MILLISECONDS));


//        multiset
        Map<String, String> kv = new HashMap<>();
        kv.put("k1", "v1");
        kv.put("k2", "v2");
        redisTemplate.opsForValue().multiSet(kv);
        List<Object> values = redisTemplate.opsForValue().multiGet(kv.keySet());
        values.forEach(o -> log.info(" mutli get {} {}", o.getClass(), o));

//        increment & decrement
        redisTemplate.opsForValue().set(key, "1");
        log.info("get class: {}", redisTemplate.opsForValue().get(key).getClass());
        redisTemplate.opsForValue().increment(key, 5);
        redisTemplate.opsForValue().decrement(key, 2);
        log.info("get: {}", redisTemplate.opsForValue().get(key));
    }


    @Test
    public void testRedisSet() {
        String key = "runoobkey";
//        先删除，否则如果key已经存在并且类型不对的话可能就有问题
        redisTemplate.delete(key);

//      SADD runoobkey v1 v2
        redisTemplate.opsForSet().add(key, IntStream.range(0, 10).mapToObj(i -> "v" + i).toArray());
//      SMEMBERS runoobkey
        log.info("members: {}", redisTemplate.opsForSet().members(key));
//        SRANDMEMBER runoobkey
        log.info("random members: {}", redisTemplate.opsForSet().randomMembers(key, 2));
        log.info("random member: {}", redisTemplate.opsForSet().randomMember(key));
        log.info("random member: {}", redisTemplate.opsForSet().randomMember(key));

//       SCARD runoobkey
        log.info("size: {}", redisTemplate.opsForSet().size(key));

//      SISMEMBER runoobkey "\"v1\"" 这里写成\"v1\"是因为对于value我用的序列化器是GenericJackson2JsonRedisSerializer
        log.info("v1 is member: {}", redisTemplate.opsForSet().isMember(key, "v1"));
        log.info("v99 is member: {}", redisTemplate.opsForSet().isMember(key, "v99"));

//      SMOVE runoobkey runoobKeyDest "\"v1\""
        final String destKey = "runoobKeyDest";
        redisTemplate.delete(destKey);
        redisTemplate.opsForSet().move(key, "v1", destKey);
        log.info("v1 is {} member: {}", key, redisTemplate.opsForSet().isMember(key, "v1"));
        log.info("v1 is {} member: {}", destKey, redisTemplate.opsForSet().isMember(destKey, "v1"));

//        SPOP runoobkey
        Object pop = (String) (redisTemplate.opsForSet().pop(key));
        log.info("pop : {} , is member {}", pop, redisTemplate.opsForSet().isMember(key, pop));

//      SREM runoobkey "\"v2\""
        redisTemplate.opsForSet().remove(key, "v2");
        log.info("v2 is {} member: {}", key, redisTemplate.opsForSet().isMember(key, "v2"));

//        SDIFF runoobkey runoobKeyDest
        log.info("different value: {}", redisTemplate.opsForSet().difference(key, destKey));
//        SINTER runoobkey runoobKeyDest
        log.info("intersect value: {}", redisTemplate.opsForSet().intersect(key, destKey));

    }


    @Test
    public void testRedisHash() {
        String key = "runoobkey";
        redisTemplate.delete(key);

//        HSET runoobkey field1 value [field value ...]
        redisTemplate.opsForHash().put(key, "field1", "value1");
        redisTemplate.opsForHash().putAll(key,
                IntStream.range(2, 10).boxed().collect(Collectors.toMap(i -> "field" + i, i -> "value" + i)));

//        HGETALL runoobkey
        log.info("all entries :{}", redisTemplate.opsForHash().entries(key));
//      HGET runoobkey field1
        log.info("hashKey: {} value {}", "field1", redisTemplate.opsForHash().get(key, "field1"));

//        HEXISTS runoobkey field1
        log.info("has key:{} {}", "field1", redisTemplate.opsForHash().hasKey(key, "field1"));
        log.info("has key:{} {}", "field99", redisTemplate.opsForHash().hasKey(key, "field99"));

//        HKEYS runoobkey
        log.info("keys: {}", redisTemplate.opsForHash().keys(key));
//        HVALS runoobkey
        log.info("keys: {}", redisTemplate.opsForHash().values(key));
//        HDEL runoobkey field3
        redisTemplate.opsForHash().delete(key, "field2");
        log.info("get field2: {}", redisTemplate.opsForHash().get(key, "field2"));

//        HINCRBY runoobkey num1 1
        redisTemplate.opsForHash().put(key, "num1", 1);
        redisTemplate.opsForHash().increment(key, "num1", 10);
        log.info("num1: {}", redisTemplate.opsForHash().get(key, "num1"));
//        HINCRBYFLOAT runoobkey num2 3.55
        redisTemplate.opsForHash().put(key, "num2", 1.22);
        redisTemplate.opsForHash().increment(key, "num2", 3.55);
        log.info("num1: {}", redisTemplate.opsForHash().get(key, "num2"));


//      HLEN runoobkey
        log.info("size: {}", redisTemplate.opsForHash().size(key));

//      操作对象
        redisTemplate.delete(key);

        redisTemplate.opsForHash().putAll(key,new ObjectMapper().convertValue(new User(1,"name"),Map.class));
        User user = new ObjectMapper().convertValue(redisTemplate.opsForHash().entries(key),User.class);
        log.info("user :{}",user);
    }


    @Test
    public void testRedisList(){
        String key = "runoobkey";
        redisTemplate.delete(key);

//        LPUSH runoobkey "\"left1\""
//        RPUSH runoobkey "\"right2\""
        redisTemplate.opsForList().leftPush(key,"left1");
        redisTemplate.opsForList().leftPushAll(key,IntStream.range(2,4).mapToObj(i->"left"+i).toArray());
        redisTemplate.opsForList().rightPushAll(key,IntStream.range(1,4).mapToObj(i->"right"+i).toArray());

//      LLEN runoobkey
        log.info("size: {}",redisTemplate.opsForList().size(key));
//        LRANGE runoobkey 0 7 不包含末尾index，即左开右闭
        log.info("range from 0 to 7: {}",redisTemplate.opsForList().range(key, 0, 7));

//        LPOP runoobkey
        log.info("left pop 1: {}",redisTemplate.opsForList().leftPop(key,1));
//        RPOP runoobkey
        log.info("right pop 2: {}",redisTemplate.opsForList().rightPop(key,2));
        log.info("range from 0 to -1: {}",redisTemplate.opsForList().range(key, 0, -1));

        // LTRIM runoobkey 1 4
        redisTemplate.opsForList().trim(key,1,4);
        log.info("range from 0 to -1: {}",redisTemplate.opsForList().range(key, 0, -1));

//        reset
        redisTemplate.delete(key);
        redisTemplate.opsForList().leftPushAll(key,IntStream.range(1,4).mapToObj(i->"left"+i).toArray());

//      LINSERT runoobkey BEFORE "\"left2\"" beforeLeft2
        redisTemplate.opsForList().leftPush(key,"left2","beforeLeft2");
//      LINSERT runoobkey AFTER "\"left2\"" afterLeft2
        redisTemplate.opsForList().rightPush(key,"left2","afterLeft2");
        log.info("range from 0 to -1: {}",redisTemplate.opsForList().range(key, 0, -1));

//      LSET runoobkey 1 "\"new1\""
        redisTemplate.opsForList().set(key,1,"new1");

//      LINDEX runoobkey 1
        log.info("get index 1: {}",redisTemplate.opsForList().index(key,1));
//      LPOS runoobkey "\"new1\""
        log.info("get indexof: {}",redisTemplate.opsForList().indexOf(key,redisTemplate.opsForList().index(key,1)));


        //        reset
        redisTemplate.delete(key);
//        BLPOP runoobkey 10
//        十秒内执行一次LPUSH runoobkey "\"left1\""
        log.info("{}",redisTemplate.opsForList().leftPop(key,10,TimeUnit.SECONDS));

    }


    @Test
    public void testRedisSerializer(){
        String key = "runoobkey";
        redisTemplate.delete(key);
//        前面发现了每个value其实都是\"aaa\"这种两侧带转义双引号的，key是不带转义双引号的，这是因为我之前使用的value的序列化方法都是基于json的

//        如果在config里对value使用new StringRedisSerializer()，那么对于所有的String都会当成普通的额字符串，不会有\"\"这些，同时，
//        设置1、2这类数字作为value的也会报错，因为StringRedisSerializer不知道怎么处理数字
        redisTemplate.opsForValue().set(key,"value1");
        redisTemplate.opsForValue().set(key,1);

//        直接set一个对象，并且可以直接获取回来并反序列化回来。
        /*
        127.0.0.1:6379> get runoobkey
"{\"@class\":\"com.example.redis_spring_client.pojo.User\",\"id\":2,\"name\":\"name\"}"
         */
        redisTemplate.opsForValue().set(key,new User(2,"name"));
        log.info("get user: {}",redisTemplate.opsForValue().get(key));

        redisTemplate.delete(key);
//        对于hash对象，是通过hashSerializer来控制的
        redisTemplate.opsForHash().put(key,"u1",new User(2,"name"));
        log.info("get user: {}", redisTemplate.opsForHash().get(key,"u1"));


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



    @Test
    public void testRedissionClient(){

//        设置了一个桶，其实就是一个kv
        RBucket<User> bucket = redissonClient.getBucket("userBucket");
        bucket.set(new User(1,"name"));
        User obj = bucket.get();

        log.info("user: {}",bucket.get());

        bucket.delete();

    }


    @Test
    public void testRedissionLock(){
        RLock lock = redissonClient.getLock("lock");
        try{
            lock.lock(100,TimeUnit.SECONDS);
        }finally {
            lock.unlock();
        }
    }
}
