package org.talk.is.cheap.project.free.flow.starter.worker.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.talk.is.cheap.project.free.flow.starter.worker.config.properties.ZKConfigProperties;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

@Slf4j
public class ClusterService {

    @Autowired
    private CuratorFramework starterCuratorZKClient;

    @Autowired
    private ZKConfigProperties zkConfigProperties;

    public String ping() {
        return "pong";
    }


    public String getRandomSchedulerId() {
        try {
            String schedulerPath = zkConfigProperties.getZookeeper().getPath().getScheduler();
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
            return null;
        }
    }
}
