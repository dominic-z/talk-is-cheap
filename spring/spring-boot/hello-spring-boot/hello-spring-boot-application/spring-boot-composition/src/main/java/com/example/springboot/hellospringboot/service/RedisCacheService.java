package com.example.springboot.hellospringboot.service;

import com.example.springboot.hellospringboot.dao.customized.CustomersDao;
import com.example.springboot.hellospringboot.domain.pojo.Customers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author dominiczhu
 * @version 1.0
 * @title MemoryCacheService
 * @date 2021/10/11 下午5:19
 */
@Service
@CacheConfig(cacheNames = "redisCache") // spring 内存注解
public class RedisCacheService {

    @Autowired
    private CustomersDao customersDao;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Cacheable(cacheNames = "custormers_", cacheManager = "halfMinuteCacheManager") // spring redis注解 在缓存就读缓存
    // 不在缓存就读db然后插入缓存
    public List<Customers> findCustomers() {

        List<Customers> customers = customersDao.selectCustomersRowBounds(1, 1);
        return customers;

    }


    @Cacheable(cacheNames = "custormers_", cacheManager = "halfMinuteCacheManager") // spring redis注解 在缓存就读缓存
    // 不在缓存就读db然后插入缓存
    public List<Customers> findCustomers2() {

        List<Customers> customers = customersDao.selectCustomersRowBounds(1, 1);
        return customers;

    }

    @CacheEvict(cacheNames = "custormers") // spring 内存注解 释放custormers缓存
    public void fireCustormers() {

    }


    @Bean
    public RedisTemplate<String, Customers> coffeeRedisTemplate(@Autowired RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Customers> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Autowired
    private RedisTemplate<String, Customers> coffeeRedisTemplate;

    private final static String CACHE_KEY = "redisCache";

    public Customers selectAndCacheCustomer(int customerNumber) {

        final HashOperations<String, String, Customers> opsForHash = coffeeRedisTemplate.opsForHash();

        final ValueOperations<String, Customers> opsForValue = coffeeRedisTemplate.opsForValue();

        if (opsForHash.hasKey(CACHE_KEY, String.valueOf(customerNumber))) {
            return opsForHash.get(CACHE_KEY, String.valueOf(customerNumber));
        }
        Customers customer = customersDao.selectByCustomerNumber(customerNumber);
        if (customer != null) {
            opsForHash.put(CACHE_KEY, String.valueOf(customerNumber), customer);
            coffeeRedisTemplate.expire(CACHE_KEY, 20, TimeUnit.SECONDS);

            opsForValue.set(String.format("%s_%d", CACHE_KEY, customerNumber), customer, 20, TimeUnit.SECONDS);
            // 去redis里查询的时候记得带双引号
        }
        return customer;

    }


}
