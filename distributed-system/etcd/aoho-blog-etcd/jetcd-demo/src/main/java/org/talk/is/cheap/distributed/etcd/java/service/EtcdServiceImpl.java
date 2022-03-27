package org.talk.is.cheap.distributed.etcd.java.service;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Response;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;


/**
 * @Description: etcd服务的实现类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2021/3/30 8:28
 */
@Slf4j
public class EtcdServiceImpl implements EtcdService {


    private volatile Client client;

    private final String endpoints;

    private final Object lock = new Object();

    public EtcdServiceImpl(String endpoints) {
        super();
        this.endpoints = endpoints;
    }

    /**
     * 将字符串转为客户端所需的ByteSequence实例
     *
     * @param val
     * @return
     */
    public static ByteSequence bytesOf(String val) {
        return ByteSequence.from(val, StandardCharsets.UTF_8);
    }

    /**
     * 新建key-value客户端实例
     *
     * @return
     */
    private KV getKVClient() {

        if (null == client) {
            synchronized (lock) {
                if (null == client) {

                    client = Client.builder().endpoints(endpoints.split(",")).build();
                }
            }
        }

        return client.getKVClient();
    }

    @Override
    public void close() {
        client.close();
        client = null;
    }

    @Override
    public Response.Header put(String key, String value) throws Exception {
        log.info("put key:{}, value:{}", key, value);
        final PutResponse putResponse = getKVClient().put(bytesOf(key), bytesOf(value)).get();
        final Response.Header header = putResponse.getHeader();
        log.info("put putResponse {}, header: {}", putResponse, header);

        return header;
    }

    @Override
    public String getSingle(String key) throws Exception {
        log.info("getSingle key {}", key);

        GetResponse getResponse = getKVClient().get(bytesOf(key)).get();
        log.info("getSingle getResponse {}", getResponse);

        return getResponse.getCount() > 0 ?
                getResponse.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8) :
                null;
    }

    @Override
    public GetResponse getRange(String key, GetOption getOption) throws Exception {
        log.info("put key:{}, getOption:{}", key, getOption);

        final GetResponse getResponse = getKVClient().get(bytesOf(key), getOption).get();
        log.info("getRange getResponse {}", getResponse);

        return getResponse;
    }

    @Override
    public long deleteSingle(String key) throws Exception {
        log.info("deleteSingle key {}", key);
        final DeleteResponse deleteResponse = getKVClient().delete(bytesOf(key)).get();
        log.info("deleteSingle deleteResponse {}", deleteResponse);

        return deleteResponse.getDeleted();
    }

    @Override
    public long deleteRange(String key, DeleteOption deleteOption) throws Exception {
        log.info("put key:{}, deleteOption:{}", key, deleteOption);

        final DeleteResponse deleteResponse = getKVClient().delete(bytesOf(key), deleteOption).get();
        log.info("deleteSingle deleteResponse {}", deleteResponse);

        return deleteResponse.getDeleted();
    }
}
