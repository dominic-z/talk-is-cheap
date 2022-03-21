import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Testing
 * @date 2021/10/11 下午7:25
 */

public class Testing {

    private static Logger logger;


    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> redisCommands;
    private RedisAsyncCommands<String, String> redisAsyncCommands;

    private GenericObjectPool<StatefulRedisConnection<String, String>> pool;

    @Before
    public void before() {
        logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

        RedisURI redisUri =
                RedisURI.builder()
                        .withHost("localhost")
                        .withPort(63790)
                        .withPassword("admin".toCharArray())
                        .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
                        .build();
        redisClient = RedisClient.create(redisUri);
        connection = redisClient.connect();


        redisCommands = connection.sync();
        redisAsyncCommands = connection.async();


        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMinIdle(3);
        poolConfig.setMaxIdle(10);
        pool = ConnectionPoolSupport.createGenericObjectPool(redisClient::connect, poolConfig);
    }

    @After
    public void after() {
        logger.info("abc");
        connection.close();
        redisClient.shutdown();
    }

    @Test
    public void testConnection() {
    }

    @Test
    public void testCommands() {

        String value = redisCommands.get("key");
        System.out.println(value);

    }

    @Test
    public void testAsyncCommands() throws ExecutionException, InterruptedException {
        RedisFuture<String> key = redisAsyncCommands.hget("map", "key");
        System.out.println(key.get());
    }

    @Test
    public void testSyncMulti() {
        redisCommands.multi();
        redisCommands.set("k1", "v1");
        redisCommands.set("k2", "v2");
        TransactionResult result = redisCommands.exec();

        for (Object r : result) {
            System.out.println(String.format("Result-%s", r));
        }
    }


    @Test
    public void usePool() throws Exception {
        StatefulRedisConnection<String, String> connection = pool.borrowObject();
        logger.info(connection.sync().get("key"));
        pool.returnObject(connection);
    }

}
