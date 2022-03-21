package config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author dominiczhu
 * @version 1.0
 * @title LettuceConfig
 * @date 2021/10/11 下午7:09
 */
public class LettuceConfig {

    public static RedisURI getRedisURI() {

        RedisURI redisUri =
                RedisURI.builder()
                        .withHost("localhost")
                        .withPort(63790)
                        .withPassword("admin".toCharArray())
                        .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
                        .build();
        return redisUri;

    }

    public static RedisClient getRedisClient() {

        RedisURI redisURI = getRedisURI();

        RedisClient redisClient = RedisClient.create(redisURI);
        return redisClient;

    }

    public static StatefulRedisConnection<String, String> getConnection() {
        RedisClient redisClient = getRedisClient();
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        return connection;
    }

    public static RedisCommands<String, String> getCommands() {
        StatefulRedisConnection<String, String> connection = getConnection();
        RedisCommands<String, String> commands = connection.sync();
        return commands;

    }


    public static RedisAsyncCommands<String, String> getAsyncCommands() {
        StatefulRedisConnection<String, String> connection = getConnection();
        RedisAsyncCommands<String, String> commands = connection.async();
        return commands;

    }

    public GenericObjectPool<StatefulRedisConnection<String, String>> getPool() {
        RedisClient redisClient = getRedisClient();

        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMinIdle(3);
        poolConfig.setMaxIdle(10);
        GenericObjectPool<StatefulRedisConnection<String, String>> pool
                = ConnectionPoolSupport.createGenericObjectPool(redisClient::connect, poolConfig);
        return pool;
    }
}
