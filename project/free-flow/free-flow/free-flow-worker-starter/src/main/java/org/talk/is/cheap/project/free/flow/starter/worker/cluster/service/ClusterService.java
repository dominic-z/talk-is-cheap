package org.talk.is.cheap.project.free.flow.starter.worker.cluster.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.talk.is.cheap.project.free.flow.common.enums.EnvType;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.utils.IPUtil;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerClusterInternalClient;
import org.talk.is.cheap.project.free.flow.starter.worker.config.properties.ZKConfigProperties;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 负责：
 * 1. 将本worker注册进zookeeper，并尝试获取当前集群中的scheduler的leader信息，为其他service与scheduler交互提供基础。
 * 2. 本worker的状态流转
 */
@Slf4j
@Service
public class ClusterService {

    private static class DistinctAddressList {
        List<String> addressList = new ArrayList<>();
        Set<String> addressSet = new HashSet<>();

        void add(String address) {
            if (addressSet.contains(address)) {
                return;
            }
            addressSet.add(address);
            addressList.add(address);
        }

        void remove(String address) {
            if (addressSet.contains(address)) {
                addressSet.remove(address);
                addressList.remove(address);
            }
        }
    }

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private CuratorFramework starterCuratorZKClient;

    @Autowired
    private ZKConfigProperties zkConfigProperties;

    @Value("${spring.application.env-type}")
    private String envType;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private int port;

    private final DistinctAddressList distinctAddressList = new DistinctAddressList();

    @Autowired
    private SchedulerClusterInternalClient schedulerClusterClient;

    private final AtomicReference<String> schedulerLeaderAddress = new AtomicReference<String>("");

    @Getter
    private String selfAbsoluteZKPath;

    private final CountDownLatch initialized = new CountDownLatch(1);


    public URI getSchedulerLeaderUri() {
        VerifyUtil.requireNotBlank(schedulerLeaderAddress.get(), "schedulerLeaderAddress is blank");
        return getUri(schedulerLeaderAddress.get());
    }

    public void registryToZK() {

        // worker需要自己注册到zk里，这样才能通过zk的心跳机制确保zk中记录的节点都是存活的
        try {
            selfAbsoluteZKPath = starterCuratorZKClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(Paths.get(zkConfigProperties.getZookeeper().getPath().getWorker().getOnline(), getSelfZKWorkerPath()).toString(),
                            getWorkerAddress().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("error when registry to zk", e);
            throw new RuntimeException(e);
        }


    }


    public String getSelfZKWorkerPath() {
        //                    zookeeper的节点不能有冒号，将192.168.1.1:2222改成192.168.1.1_2222
        return getWorkerAddress().replace(":", "_");
    }

    /**
     * @return
     */
    public String getWorkerAddress() {
        String address = EnvType.getByName(envType) == EnvType.CONTAINER ? System.getenv("CONTAINER_NAME") : IPUtil.getMainIP();
        address += ":" + port;
        return address;
    }

    public void listenAndSetSchedulerLeader() {
        listenSchedulerClusterChange();

        // 至少得获得一个scheduler的信息才可以，也就是说，updateSchedulerLeader得至少执行一次，否则方法不可以返回
        try {
            initialized.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 监听leader选举路径，当leader选举路径发生改变时，就尝试重新获取leader
     */
    private void listenSchedulerClusterChange() {
        String electionPath = zkConfigProperties.getZookeeper().getPath().getScheduler().getElection();
        CuratorCache curatorCache = CuratorCache.builder(starterCuratorZKClient, electionPath).build();
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forPathChildrenCache(electionPath, starterCuratorZKClient, new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                        log.info("leader election event: {}", event);
                        if(event.getData()!=null){
                            String schedulerAddress = new String(event.getData().getData(), StandardCharsets.UTF_8);
                            switch (event.getType()) {
                                case CHILD_ADDED:
                                    distinctAddressList.add(schedulerAddress);
                                    break;
                                case CHILD_REMOVED:
                                    distinctAddressList.remove(schedulerAddress);
                                    break;
                            }
                        }
                        updateSchedulerLeader();
                    }
                }).build();

        curatorCache.listenable().addListener(listener);
        curatorCache.start();
    }

    private void updateSchedulerLeader() {
        try {
            String randomSchedulerAddress = getRandomSchedulerAddress();
            log.info("random scheduler: {}", randomSchedulerAddress);
            URI uri = getUri(randomSchedulerAddress);
            HttpBody<String> resp = schedulerClusterClient.getLeaderAddress(uri);
            log.info("getLeaderResp: {}", resp);
            initialized.countDown();
            this.schedulerLeaderAddress.compareAndExchange(this.schedulerLeaderAddress.get(), resp.getData());
        } catch (Exception e) {
            log.error("error when get to scheduler leader", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 从zk路径里随机后去一个scheduler的id
     *
     * @return
     */
    private String getRandomSchedulerAddress() {
        try {
            VerifyUtil.requireFalse(distinctAddressList.addressList.isEmpty(), "can't find any scheduler host");

            return distinctAddressList.addressList.get(new Random().nextInt(distinctAddressList.addressList.size()));
        } catch (Exception e) {
            log.error("can't find leader host", e);
            throw new RuntimeException(e);
        }
    }

    private static URI getUri(String randomSchedulerAddress) {
        return UriComponentsBuilder.fromHttpUrl("http://" + randomSchedulerAddress).build().toUri();
    }

    public URI getRandomSchedulerURI(){
        return getUri(getRandomSchedulerAddress());
    }


    // todo: 支持设置最长等待时长
    public void terminate() {

        try {
            starterCuratorZKClient.delete()
                    .forPath(this.selfAbsoluteZKPath);
            String terminatingPath = starterCuratorZKClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(Paths.get(zkConfigProperties.getZookeeper().getPath().getWorker().getTerminating(), getSelfZKWorkerPath()).toString(),
                            getWorkerAddress().getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("error when terminate", e);
            throw new RuntimeException(e);
        }


    }

}
