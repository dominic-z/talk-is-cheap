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
import org.springframework.web.util.UriComponentsBuilder;
import org.talk.is.cheap.project.free.flow.common.enums.EnvType;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.utils.IPUtil;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerClusterClient;
import org.talk.is.cheap.project.free.flow.starter.worker.config.properties.ZKConfigProperties;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;


/**
 * 负责：
 * 1. 将本worker注册进zookeeper，并尝试获取当前集群中的scheduler的leader信息，为其他service与scheduler交互提供基础。
 * 2. 本worker的状态流转
 */
@Slf4j
public class ClusterService {

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

    @Autowired
    private SchedulerClusterClient schedulerClusterClient;

    private String schedulerLeaderId;

    @Getter
    private String selfAbsoluteZKPath;


    public URI getSchedulerLeaderUri() {
        VerifyUtil.shallNotBeBlank(schedulerLeaderId, "schedulerLeaderId is blank");
        return UriComponentsBuilder.fromHttpUrl("http://" + schedulerLeaderId).build().toUri();
    }

    public void registryToZK() {

        // worker需要自己注册到zk里，这样才能通过zk的心跳机制确保zk中记录的节点都是存活的
        try {
            selfAbsoluteZKPath = starterCuratorZKClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(Paths.get(zkConfigProperties.getZookeeper().getPath().getWorker().getRunnable(), getSelfZKWorkerPath()).toString(),
                            getWorkerId().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("error when registry to zk", e);
            throw new RuntimeException(e);
        }


        listenAndSetSchedulerLeader();

    }


    public String getSelfZKWorkerPath() {
        //                    zookeeper的节点不能有冒号，将192.168.1.1:2222改成192.168.1.1_2222
        return getWorkerId().replace(":", "_");
    }

    /**
     * id用来直接做http请求的目的，一般是ip，或者hostname
     * @return
     */
    public String getWorkerId() {
        String workerId = EnvType.getByName(envType) == EnvType.CONTAINER ? System.getenv("CONTAINER_NAME") : IPUtil.getMainIP();
        workerId += ":" + port;
        return workerId;
    }

    public void listenAndSetSchedulerLeader() {
        listenSchedulerLeaderChange();

        try {
            String randomSchedulerId = getRandomSchedulerId();
            log.info("random scheduler: {}", randomSchedulerId);
            URI uri = UriComponentsBuilder.fromHttpUrl("http://" + randomSchedulerId).build().toUri();
            HttpBody<String> resp = schedulerClusterClient.getLeaderId(uri);
            log.info("getLeaderResp: {}", resp);
            this.schedulerLeaderId = resp.getData();
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
    private String getRandomSchedulerId() {
        try {
            String schedulerPath = zkConfigProperties.getZookeeper().getPath().getScheduler().getElection();
            List<String> schedulerElectionKeys =
                    starterCuratorZKClient.getChildren().forPath(schedulerPath);
            if (schedulerElectionKeys.isEmpty()) {
                log.error("no scheduler");
                return null;
            }
            // 随意找一个scheduler就可以，scheduler本身有gateway
            // todo: 找到真的leader
            String randomSchedulerNode = schedulerElectionKeys.get(new Random().nextInt(schedulerElectionKeys.size()));
            log.info("randomSchedulerNode: {}", randomSchedulerNode);
            byte[] bytes = starterCuratorZKClient.getData().forPath(schedulerPath + "/" + randomSchedulerNode);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("can't find leader host", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 监听leader选举路径，当leader选举路径发生改变时，就尝试重新获取leader
     */
    private void listenSchedulerLeaderChange() {
        String electionPath = zkConfigProperties.getZookeeper().getPath().getScheduler().getElection();
        CuratorCache curatorCache = CuratorCache.builder(starterCuratorZKClient, electionPath).build();
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forPathChildrenCache(electionPath, starterCuratorZKClient, new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                        log.info("leader election event: {}", event);
                        listenAndSetSchedulerLeader();
                    }
                }).build();

        curatorCache.listenable().addListener(listener);
        curatorCache.start();
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
                            getWorkerId().getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("error when terminate", e);
            throw new RuntimeException(e);
        }


    }

}
