package com.example.redis_spring_client.service;

import com.example.redis_spring_client.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CacheService {

    @Cacheable(cacheNames = "users", key = "#id")
    public User getUserById(Long id) {
        // 模拟从数据库中获取用户信息
        log.info("Fetching user from database with id: {}, it's a time-taking operation", id);
        return new User(id, "John Doe");
    }

    @CacheEvict(cacheNames = "users", key = "#id")
    public void deleteUser(Long id) {
        // 模拟从数据库中删除用户信息
        log.info("Deleting user from database with id: " + id);
    }

}
