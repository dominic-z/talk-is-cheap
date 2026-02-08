package org.talk.is.cheap.project.free.flow.starter.repository.config;

import io.lettuce.core.resource.DefaultClientResources;
import jakarta.annotation.PreDestroy;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.talk.is.cheap.project.free.flow.common.utils.YamlUtil;
import org.talk.is.cheap.project.free.flow.starter.repository.config.properties.RedisProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;


/**
 * 不干扰RedisAutoConfiguration自己的配置
 */
@AutoConfiguration(afterName = {"org.redisson.spring.starter.RedissonAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration"},
        beforeName = {"org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration"}
)
public class RedisAutoConfig {


    private static final String REDIS_CONFIG_FILENAME = "redis-config.yaml";

    public static final String REDIS_PROPERTIES_BEAN_NAME = "repoRedisProperties";
    public static final String REDIS_CONNECTION_FACTORY_BEAN_NAME = "repoRedisConnectionFactory";
    public static final String REDIS_TEMPLATE = "repoRedisTemplate";
    public static final String STRING_REDIS_TEMPLATE = "repoStringRedisTemplate";
    public static final String REDISSON_CLIENT = "repoRedissonClient";

    private volatile LettuceConnectionFactory lettuceConnectionFactory;

    @Bean(REDIS_PROPERTIES_BEAN_NAME)
    public RedisProperties redisProperties() throws IOException {
        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        Resource resource = patternResolver.getResource("classpath:/" + REDIS_CONFIG_FILENAME);
        if (!resource.exists()) {
            throw new FileNotFoundException(REDIS_CONFIG_FILENAME + " not found");
        }
        RedisProperties redisProperties = YamlUtil.loadFile(resource.getURL(), RedisProperties.class);
        return redisProperties;
    }

    private GenericObjectPoolConfig<?> getPoolConfig(RedisProperties redisProperties) {
        GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(redisProperties.getPool().getMaxActive());
        config.setMaxIdle(redisProperties.getPool().getMaxIdle());
        config.setMinIdle(redisProperties.getPool().getMinIdle());
        if (redisProperties.getPool().getMaxWait() != null) {
            config.setMaxWait(Duration.ofSeconds(redisProperties.getPool().getMaxWait()));
        }
        return config;
    }

    private LettucePoolingClientConfiguration createClientConfig(RedisProperties redisProperties) {
        DefaultClientResources clientResources = DefaultClientResources.builder().build();

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder =
                LettucePoolingClientConfiguration.builder().poolConfig(getPoolConfig(redisProperties));

        if (redisProperties.getTimeout() != null) {
            builder.commandTimeout(Duration.ofSeconds(redisProperties.getTimeout()));
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


//    废了半天牛劲，还是不能创建这个bean，如果创建了，容器里就有两个RedisConnectionFactory了，
//    而org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration这个自动配置类会触发，触发的时候他不知道该指定哪个bean，所以报错
//    expected single matching bean but found 2: redisConnectionFactory,repoRedisConnectionFactory
//    所以qtmd我直接不注册bean就好了
//    @Bean(REDIS_CONNECTION_FACTORY_BEAN_NAME)
//    @DependsOn("reactiveRedisTemplate")
//    public LettuceConnectionFactory redisConnectionFactory(@Qualifier(REDIS_PROPERTIES_BEAN_NAME) RedisProperties redisProperties) {
//        LettucePoolingClientConfiguration clientConfig = createClientConfig(redisProperties);
//        RedisStandaloneConfiguration standaloneConfig = getStandaloneConfig(redisProperties);
//
//
//        return new LettuceConnectionFactory(standaloneConfig, clientConfig);
//    }


    private LettuceConnectionFactory getRedisConnectionFactory(RedisProperties redisProperties) {
        if (lettuceConnectionFactory != null) {
            return lettuceConnectionFactory;
        }

        synchronized(this) {
            if(lettuceConnectionFactory!=null){
                return lettuceConnectionFactory;
            }

            LettucePoolingClientConfiguration clientConfig = createClientConfig(redisProperties);
            RedisStandaloneConfiguration standaloneConfig = getStandaloneConfig(redisProperties);
            lettuceConnectionFactory = new LettuceConnectionFactory(standaloneConfig, clientConfig);
            // 因为LettuceConnectionFactory实现了Lifecycle接口
            // Spring 容器完成所有 Bean 的初始化（实例化、属性注入、初始化回调如 @PostConstruct）后，调用所有 Lifecycle 实现类的 start() 方法。
            // 而因为我没有将lettuceConnectionFactory注册为bean，因此我得手动调用他的start方法
            // java.lang.IllegalStateException: LettuceConnectionFactory has been CREATED. Use start() to initialize it
            lettuceConnectionFactory.start();
        }

        return lettuceConnectionFactory;
    }

    @PreDestroy
    public void destroy(){
        this.lettuceConnectionFactory.stop();
    }

    @Bean(REDIS_TEMPLATE)
    public RedisTemplate<String, Object> redisTemplate(@Qualifier(REDIS_PROPERTIES_BEAN_NAME) RedisProperties redisProperties) {
//        connectionFactory.getConnection()
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(getRedisConnectionFactory(redisProperties));
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());


//        对hash的key的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    @Bean(STRING_REDIS_TEMPLATE)
    public StringRedisTemplate stringRedisTemplate(@Qualifier(REDIS_PROPERTIES_BEAN_NAME) RedisProperties redisProperties) {
//        connectionFactory.getConnection()
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(getRedisConnectionFactory(redisProperties));
        return template;
    }


    @Bean(REDISSON_CLIENT)
    public RedissonClient redissonClient(@Qualifier(REDIS_PROPERTIES_BEAN_NAME) RedisProperties redisProperties) {
        Config config = new Config();
        config.useSingleServer().setAddress(String.format("redis://%s:%s", redisProperties.getHost(), redisProperties.getPort()))
                .setPassword(redisProperties.getPassword());
        config.setCodec(new JsonJacksonCodec());
        return Redisson.create(config);
    }
}
