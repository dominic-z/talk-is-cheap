package org.talk.is.cheap.distributed.etcd.java.discovery;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Discovery
 * @date 2022/3/28 3:32 下午
 */
@Slf4j
public class Discovery {

    private final Client client;
    private final String endpoints;
    private final Object lock = new Object();
    private final Map<String, String> serverMap = new ConcurrentHashMap<>();
    private final ThreadPoolExecutor threadPoolExecutor;

    /**
     * 发现服务类信息初始化
     *
     * @param endpoints：监听端点，包含ip和端口，如："http://localhost:2379“，多个端点则使用逗号分割， 比如：”http://localhost:2379,http://192.168
     *                                                                      .2.1:2330“
     */
    public Discovery(String endpoints) {
        this.endpoints = endpoints;
        this.client = Client.builder().endpoints(endpoints.split(",")).build();
        this.threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }

    public void watchService(String prefixAddress) throws ExecutionException, InterruptedException {
        log.info("watchService: {}", prefixAddress);
        watch(prefixAddress); // 应当先watch，然后再初始化，否则会出现有些节点无法被注册进去

        final GetResponse getResponse = client.getKVClient().get(
                ByteSequence.from(prefixAddress, StandardCharsets.UTF_8),
                GetOption.newBuilder().isPrefix(true).build()).get();
        log.info("getResponse: {}", getResponse);

        final List<KeyValue> kvs = getResponse.getKvs();
        //获取当前前缀下的服务并存储
        for (KeyValue kv : kvs) {
            final String key = kv.getKey().toString(StandardCharsets.UTF_8);
            final String value = kv.getValue().toString(StandardCharsets.UTF_8);
            log.info("set serverMap: key: {}, value: {}", key, value);
            serverMap.put(key, value);
        }

        log.info("initial servers: {}", serverMap);

    }

    private void watch(String prefixAddress) {
        log.info("watch: {}", prefixAddress);


        final WatchOption watchOption = WatchOption.newBuilder().isPrefix(true).build();
        //实例化一个监听对象，当监听的key发生变化时会被调用
        final Watch.Listener listener = Watch.listener(watchResponse -> {
            log.info("watchResponse: {}", watchResponse);
            for (WatchEvent event : watchResponse.getEvents()) {

                final WatchEvent.EventType eventType = event.getEventType();
                KeyValue kv = event.getKeyValue();
                final String key = kv.getKey().toString(StandardCharsets.UTF_8);
                final String value = kv.getValue().toString(StandardCharsets.UTF_8);
                log.info("type: {}, key: {}, value: {}", eventType, key, value);

                switch (eventType) {
                    case PUT: //修改或者新增
                        serverMap.put(key, value);
                        break;
                    case DELETE:
                        serverMap.remove(key);
                }
            }
            log.info("watch servers: {}", serverMap);

        });

        client.getWatchClient().watch(ByteSequence.from(prefixAddress, StandardCharsets.UTF_8), watchOption, listener);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final String endpoints = "http://localhost:2379";
        Discovery discovery = new Discovery(endpoints);
        discovery.watchService("/web/");
        discovery.watchService("/grpc/");
    }
}
