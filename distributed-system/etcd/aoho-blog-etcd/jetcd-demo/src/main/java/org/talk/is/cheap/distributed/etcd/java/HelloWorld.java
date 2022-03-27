package org.talk.is.cheap.distributed.etcd.java;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

/**
 * @author dominiczhu
 * @version 1.0
 * @title HelloWorld
 * @date 2022/3/27 11:48 上午
 */
@Slf4j
public class HelloWorld {

    /**
     * 新建key-value客户端实例
     * @return
     */
    private KV getKVClient(){
        String endpoints = "http://localhost:2379";
        Client client = Client.builder().endpoints(endpoints.split(",")).build();
        return client.getKVClient();
    }

    /**
     * 将字符串转为客户端所需的ByteSequence实例
     * @param val
     * @return
     */
    private static ByteSequence bytesOf(String val) {
        return ByteSequence.from(val, StandardCharsets.UTF_8);
    }

    /**
     * 查询指定键对应的值
     * @param key
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public String get(String key) throws ExecutionException, InterruptedException{
        log.info("start get, key [{}]", key);
        GetResponse response = getKVClient().get(bytesOf(key)).get();

        if (response.getKvs().isEmpty()) {
            log.error("empty value of key [{}]", key);
            return null;
        }

        String value = response.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
        log.info("finish get, key [{}], value [{}]", key, value);
        return value;
    }

    /**
     * 创建键值对
     * @param key
     * @param value
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public PutResponse put(String key, String value) throws ExecutionException, InterruptedException {
        log.info("start put, key [{}], value [{}]", key, value);
        return getKVClient().put(bytesOf(key), bytesOf(value)).get();
    }
}
