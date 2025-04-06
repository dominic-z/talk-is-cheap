package org.talk.is.cheap.distributed.etcd.java.discovery;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Response;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.PutOption;
import io.grpc.stub.CallStreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Register
 * @date 2022/3/28 3:12 下午
 */
@Slf4j
public class Register {
    private volatile Client client;
    private String endpoints;
    private final Object lock = new Object();

    public Register(String endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * 新建客户端实例
     *
     * @return
     */
    private Client getClient() {
        if (null == client) {
            synchronized (lock) {
                if (null == client) {
                    client = Client.builder().endpoints(endpoints.split(",")).build();
                }
            }
        }
        return client;
    }

    /**
     * 新建key-value客户端实例
     *
     * @return
     */
    private KV getKVClient() {
        return getClient().getKVClient();
    }

    public void close() {
        client.close();
        client = null;
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

    public Response.Header put(String key, String value) throws ExecutionException, InterruptedException {
        log.info("put key:{}, value:{}", key, value);
        final PutResponse putResponse = getKVClient().put(bytesOf(key), bytesOf(value)).get();
        log.info("putResponse: {}", putResponse);
        return putResponse.getHeader();
    }

    public void putWithLease(String key, String value) throws Exception {

        final Lease leaseClient = getClient().getLeaseClient();
        leaseClient.grant(60).thenAccept(result -> {
            // 租约ID
            long leaseId = result.getID();

            // 准备好put操作的client
            KV kvClient = getClient().getKVClient();

            // put操作时的可选项，在这里指定租约ID
            PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();
            // put操作
            kvClient.put(bytesOf(key), bytesOf(value), putOption)
                    .thenAccept(putResponse -> {
                        // put操作完成后，再设置无限续租的操作
                        leaseClient.keepAlive(leaseId, new CallStreamObserver<LeaseKeepAliveResponse>() {
                            @Override
                            public boolean isReady() {
                                return false;
                            }

                            @Override
                            public void setOnReadyHandler(Runnable onReadyHandler) {

                            }

                            @Override
                            public void disableAutoInboundFlowControl() {

                            }

                            @Override
                            public void request(int count) {
                            }

                            @Override
                            public void setMessageCompression(boolean enable) {

                            }

                            /**
                             * 每次续租操作完成后，该方法都会被调用
                             * @param resp
                             */
                            @Override
                            public void onNext(LeaseKeepAliveResponse resp) {

                                log.info("续租完成,{}", resp);

                            }

                            @Override
                            public void onError(Throwable t) {
                                log.error("续租失败", t);
                            }

                            @Override
                            public void onCompleted() {
                                // 大致看了一眼源码，好像不会有什么地方会调用到这里
                                log.info("onCompleted方法执行");
                            }
                        });
                    });
        });

    }
    public static void main(String[] args) {
        Register register = new Register("http://localhost:2379");
        String key = "/web/node0";
        String value = "localhost:8080";
//        try {
//            register.put(key, value);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            register.putWithLease(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
