package com.example.springboot.hellospringboot.aspect.redis;

import com.example.springboot.hellospringboot.domain.anno.redis.SemaphoreLimit;
import com.example.springboot.hellospringboot.domain.enums.redis.SemaphoreLimitType;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * @author dominiczhu
 * @version 1.0
 * @title LimitInterceptor
 * @date 2022/1/16 11:11 上午
 */
@Aspect
@Configuration
@Slf4j
public class LimitInterceptor {

    private static final String UNKNOWN = "unknown";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Around("execution(public * *(..)) && @annotation(com.example.springboot.hellospringboot.domain.anno.redis" +
            ".SemaphoreLimit)")
    public Object interceptor(ProceedingJoinPoint pjp) {
        log.info("限流器interceptor 启动");

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        SemaphoreLimit limitAnnotation = method.getAnnotation(SemaphoreLimit.class);
        SemaphoreLimitType limitType = limitAnnotation.limitType();
        String name = limitAnnotation.name();
        String key = limitAnnotation.key();
        int limitPeriod = limitAnnotation.period();
        int limitCount = limitAnnotation.count();

        if (StringUtils.isBlank(key)) {
            key = method.getName().toUpperCase(Locale.ROOT);
        }
        /**
         * 根据限流类型获取不同的key ,如果不传我们会以方法名作为key
         */
        switch (limitType) {
            case IP:
                key = key + ":" + getIpAddress();
                break;
            case CUSTOMER:
                key = key + ":" + limitAnnotation.key();
                break;
            default:
        }

        ImmutableList<String> keys = ImmutableList.of(limitAnnotation.prefix() + key);
        try {
            String luaScript = buildLuaScript();
            RedisScript<String> redisScript = new DefaultRedisScript<>(luaScript, String.class);
            String count = stringRedisTemplate.execute(redisScript, keys, String.valueOf(limitCount),
                    String.valueOf(limitPeriod));
            log.info("Access try count is {} for name={} and key = {}", count, name, key);
            if (StringUtils.isNumeric(count) && Integer.parseInt(count) <= limitCount) {
                return pjp.proceed();
            } else {
                log.error("You have been dragged into the blacklist");
                return null;
//                throw new RuntimeException("You have been dragged into the blacklist");
            }
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException("server exception");
        }
    }

    /**
     * @author fu
     * @description 编写 redis Lua 限流脚本
     * @date 2020/4/8 13:24
     */
    public String buildLuaScript() {
        StringBuilder lua = new StringBuilder();
        lua.append("local c");
        lua.append("\nc = redis.call('get',KEYS[1])");
        // 调用不超过最大值，则直接返回
        lua.append("\nif c and tonumber(c) > tonumber(ARGV[1]) then");
        lua.append("\nreturn c;");
        lua.append("\nend");
        // 执行计算器自加
        lua.append("\nredis.call('incr',KEYS[1])");
        lua.append("\nif c == 1 then");
        // 从第一次调用开始限流，设置对应键值的过期
        lua.append("\nredis.call('expire',KEYS[1],tonumber(ARGV[2]))");
        lua.append("\nend");
        lua.append("\nreturn redis.call('get',KEYS[1]);");
        return lua.toString();
    }

    /**
     * @author fu
     * @description 获取id地址
     * @date 2020/4/8 13:24
     */
    public String getIpAddress() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
