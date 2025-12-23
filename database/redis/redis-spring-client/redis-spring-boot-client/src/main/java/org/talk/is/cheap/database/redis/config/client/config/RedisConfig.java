package org.talk.is.cheap.database.redis.config.client.config;


import io.lettuce.core.resource.DefaultClientResources;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.talk.is.cheap.database.redis.config.client.config.properties.RedisProperties;

@Configuration
@Slf4j
@EnableConfigurationProperties(RedisProperties.class)

public class RedisConfig {


    @Autowired
    private RedisProperties redisProperties;


    //参考org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration.PoolBuilderFactory.getPoolConfig
    private GenericObjectPoolConfig<?> getPoolConfig() {
        GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(redisProperties.getPool().getMaxActive());
        config.setMaxIdle(redisProperties.getPool().getMaxIdle());
        config.setMinIdle(redisProperties.getPool().getMinIdle());
        if (redisProperties.getPool().getMaxWait() != null) {
            config.setMaxWait(redisProperties.getPool().getMaxWait());
        }
        return config;
    }

    /**
     * 参考org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration.getLettuceClientConfiguration
     * 但是我没有那么多customizer
     * 注意，没有做ssl的适配，如果使用ssl协议，还得细研究。。。
     * @return
     */
    private LettucePoolingClientConfiguration createClientConfig(){
        DefaultClientResources clientResources = DefaultClientResources.builder().build();

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder =
                LettucePoolingClientConfiguration.builder().poolConfig(getPoolConfig());

        if (redisProperties.getTimeout() != null) {
            builder.commandTimeout(redisProperties.getTimeout());
        }

        builder.clientResources(clientResources);


        return builder.build();
    }

    private RedisStandaloneConfiguration getStandaloneConfig() {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(this.redisProperties.getHost());
        config.setPort(this.redisProperties.getPort());
        config.setUsername(this.redisProperties.getUsername());
        config.setPassword(RedisPassword.of(this.redisProperties.getPassword()));
        config.setDatabase(this.redisProperties.getDatabase());
        return config;
    }

    public final static String REDIS_CONNECTION_FACTORY = "myRedisConnectionFactory";
    public final static String REDIS_TEMPLATE = "myRedisTemplate";
    public final static String REDISSON_CLIENT = "myRedissonClient";

    /**
     * 参考org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration#createConnectionFactory(org.springframework.beans.factory.ObjectProvider, org.springframework.beans.factory.ObjectProvider, io.lettuce.core.resource.ClientResources)
     * 只做了单机的配置方法
     * @return
     */
    @Bean(REDIS_CONNECTION_FACTORY)
    public LettuceConnectionFactory redisConnectionFactory() {
        LettucePoolingClientConfiguration clientConfig = createClientConfig();
        RedisStandaloneConfiguration standaloneConfig = getStandaloneConfig();


        return new LettuceConnectionFactory(standaloneConfig, clientConfig);
    }

    @Bean(REDIS_TEMPLATE)
    public RedisTemplate<String, Object> redisTemplate(@Qualifier(REDIS_CONNECTION_FACTORY) RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

//        对hash的key的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }



    @Bean(REDISSON_CLIENT)
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress(String.format("redis://%s:%s",redisProperties.getHost(),redisProperties.getPort()))
                .setPassword(redisProperties.getPassword());
        config.setCodec(new JsonJacksonCodec());
        return Redisson.create(config);
    }
}
