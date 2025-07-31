package org.talk.is.cheap.project.free.flow.starter.worker.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;
import org.talk.is.cheap.project.free.flow.common.enums.EnvType;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.utils.IPUtils;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerClusterClient;
import org.talk.is.cheap.project.free.flow.starter.worker.config.properties.ZKConfigProperties;
import org.talk.is.cheap.project.free.flow.starter.worker.domain.enums.WorkerStatus;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

@Slf4j
public class ClusterService {

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

    @Getter
    private String schedulerLeaderId;

    @Getter
    private String zkWorkerPath;

    @Getter
    private WorkerStatus workerStatus;

    public void registryWorker() {

        // worker需要自己注册到zk里，这样才能通过zk的心跳机制确保zk中记录的节点都是存活的
        try {
            zkWorkerPath = starterCuratorZKClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(Paths.get(zkConfigProperties.getZookeeper().getPath().getWorker().getRunnable(), applicationName).toString(),
                            getWorkerId().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("error when registry to zk", e);
            throw new RuntimeException(e);
        }


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

        workerStatus = WorkerStatus.RUNNABLE;

        // 改成scheduler主动链接
//        WorkerRegistryReq req = new WorkerRegistryReq();
//
//        req.setData(WorkerRegistryReq.Data.builder().workerId(workerId).zkNodePath(nodePath).build());
//        schedulerClusterClient.registryWorker(uri, req);
    }

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


    public String getWorkerId() {
        String workerId = EnvType.getByName(envType) == EnvType.CONTAINER ? System.getenv("CONTAINER_NAME") : IPUtils.getMainIP();
        workerId += ":" + port;
        return workerId;
    }


    // todo: 支持设置最长等待时长
    public void terminate() {

        try {
            starterCuratorZKClient.delete()
                    .forPath(this.zkWorkerPath);
            String terminatingPath = starterCuratorZKClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(Paths.get(zkConfigProperties.getZookeeper().getPath().getWorker().getTerminating(), applicationName).toString(),
                            getWorkerId().getBytes(StandardCharsets.UTF_8));
            this.workerStatus = WorkerStatus.TERMINATING;

        } catch (Exception e) {
            log.error("error when terminate", e);
            throw new RuntimeException(e);
        }


    }

}
