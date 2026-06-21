package org.talk.is.cheap.database.redis.autoconfig.client.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@Slf4j
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // 设置缓存过期时间为10分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
//
//    @Bean
//    public LettuceConnectionFactory redisConnectionFactory() {
//        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration()
//                .clusterNode("127.0.0.1", 6379);
//
//        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
//                .clientOptions(ClientOptions.builder()
//                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
//                        .build())
//                .build();
//
//        return new LettuceConnectionFactory(clusterConfig, clientConfig);
//    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//        connectionFactory.getConnection()
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置序列器，这是为了让一个java对象能够转成redis能够存储的数据，就是将java对象转成字符串
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

//        等同于使用new GenericJackson2JsonRedisSerializer()，会将类型信息作为一个json的一个字段记录进redis
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        // 方法过期，改为下面代码
////        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance ,
//                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
//        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper,Object.class);
//        template.setValueSerializer(jackson2JsonRedisSerializer);

//        对string类型的value使用字符串序列化器
//        template.setValueSerializer(new StringRedisSerializer());

//        对hash的key的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }


    /**
     * 将所有key-value都视作原始的字符串存储，看StringRedisSerializer的代码就能理解了
     * @param connectionFactory
     * @return
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
//        connectionFactory.getConnection()
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);

        return template;
    }

    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private String redisPort;
    @Value("${spring.data.redis.password}")
    private String redisPwd;

    /**
     * redisson的自动配置在org.redisson.spring.starter.RedissonAutoConfiguration
     * 主要在yaml里写一些配置项目，我没写，直接手动写个client
     * @return
     */
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        // 1. 基础线程池配置（全局）
        config.setThreads(16); // 处理Redis命令的工作线程数，默认=CPU核心数*2
        config.setNettyThreads(32); // Netty IO线程数，默认=CPU核心数*2

        config.useSingleServer()
                .setAddress(String.format("redis://%s:%s",redisHost,redisPort))
                .setPassword(redisPwd)
                // ========== 核心连接池配置 ==========
                .setConnectionPoolSize(64) // 连接池最大连接数，默认=64
                .setConnectionMinimumIdleSize(10) // 连接池最小空闲连接数，默认=10
                .setIdleConnectionTimeout(10000) // 空闲连接超时时间（毫秒），默认=10000
                .setConnectTimeout(3000) // 连接超时时间（毫秒），默认=10000
                .setTimeout(3000) // Redis命令超时时间（毫秒），默认=3000
                .setRetryAttempts(3) // 命令重试次数，默认=3
                .setRetryInterval(1500) // 重试间隔时间（毫秒），默认=1500
                .setPingConnectionInterval(60000) // 定期检查连接有效性的间隔（毫秒），0=禁用，默认=60000
                .setSubscriptionConnectionPoolSize(50) // 订阅连接池大小，默认=50
                .setSubscriptionConnectionMinimumIdleSize(1) // 订阅连接池最小空闲数，默认=1
                .setDnsMonitoringInterval(5000); // DNS监控刷新间隔（毫秒），默认=5000
        ;
        config.setCodec(new JsonJacksonCodec());
        return Redisson.create(config);
    }
}
