package org.talk.is.cheap.distributed.etcd.java;


import io.etcd.jetcd.kv.PutResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.concurrent.ExecutionException;

/**
 * @author dominiczhu
 * @version 1.0
 * @title HelloWorldTest
 * @date 2022/3/27 12:00 下午
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HelloWorldTest {
    // 用与测试的键
    private static final String KEY = "/abc/foo-" + System.currentTimeMillis();

    // 用于测试的值
    private static final String VALUE = "/abc/foo";

    /**
     * 之后可以通过命令行<code>etcdctl get --prefix /abc/foo</code>
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    @Order(2)
    public void get() throws ExecutionException, InterruptedException {
        String getResult = new HelloWorld().get(KEY);
        log.info("getResult: {}", getResult);
        Assertions.assertEquals(VALUE, getResult);
    }

    @Test
    @Order(1)
    void put() throws ExecutionException, InterruptedException {
        PutResponse putResponse = new HelloWorld().put(KEY, VALUE);
        log.info("putResponse: {}", putResponse);

        Assertions.assertNotNull(putResponse);
        Assertions.assertNotNull(putResponse.getHeader());
    }
}
