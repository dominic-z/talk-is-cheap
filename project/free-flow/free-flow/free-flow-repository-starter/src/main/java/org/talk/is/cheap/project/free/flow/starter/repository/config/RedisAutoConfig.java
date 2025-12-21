package org.talk.is.cheap.project.free.flow.starter.repository.config;

import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.talk.is.cheap.project.free.flow.common.utils.YamlUtil;
import org.talk.is.cheap.project.free.flow.starter.repository.config.properties.RedisProperties;

import java.io.FileNotFoundException;
import java.io.IOException;


@AutoConfiguration(afterName = {"org.redisson.spring.starter.RedissonAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration"})
public class RedisAutoConfig {


    private static final String REDIS_CONFIG_FILENAME = "redis-config.yaml";

    @Bean
    public RedisProperties redisProperties() throws IOException {
        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        Resource resource = patternResolver.getResource("classpath:/" + REDIS_CONFIG_FILENAME);
        if (!resource.exists()) {
            throw new FileNotFoundException(REDIS_CONFIG_FILENAME + " not found");
        }
        return YamlUtil.load(resource.getURL(), RedisProperties.class);
    }

    private GenericObjectPoolConfig<?> getPoolConfig(RedisProperties redisProperties) {
        GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(redisProperties.getPool().getMaxActive());
        config.setMaxIdle(redisProperties.getPool().getMaxIdle());
        config.setMinIdle(redisProperties.getPool().getMinIdle());
        if (redisProperties.getPool().getMaxWait() != null) {
            config.setMaxWait(redisProperties.getPool().getMaxWait());
        }
        return config;
    }

    private LettucePoolingClientConfiguration createClientConfig(RedisProperties redisProperties) {
        DefaultClientResources clientResources = DefaultClientResources.builder().build();

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder =
                LettucePoolingClientConfiguration.builder().poolConfig(getPoolConfig(redisProperties));

        if (redisProperties.getTimeout() != null) {
            builder.commandTimeout(redisProperties.getTimeout());
        }

        builder.clientResources(clientResources);


        return builder.build();
    }

    private RedisStandaloneConfiguration getStandaloneConfig(RedisProperties redisProperties) {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost());
        config.setPort(redisProperties.getPort());
        config.setUsername(redisProperties.getUsername());
        config.setPassword(RedisPassword.of(redisProperties.getPassword()));
        config.setDatabase(redisProperties.getDatabase());
        return config;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        LettucePoolingClientConfiguration clientConfig = createClientConfig(redisProperties);
        RedisStandaloneConfiguration standaloneConfig = getStandaloneConfig(redisProperties);


        return new LettuceConnectionFactory(standaloneConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//        connectionFactory.getConnection()
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());


//        对hash的key的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }


    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        config.useSingleServer().setAddress(String.format("redis://%s:%s", redisProperties.getHost(), redisProperties.getPort()))
                .setPassword(redisProperties.getPassword());
        config.setCodec(new JsonJacksonCodec());
        return Redisson.create(config);
    }
}
