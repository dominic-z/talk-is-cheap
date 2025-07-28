package org.talk.is.cheap.project.free.flow.starter.worker.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;
import org.talk.is.cheap.project.free.common.enums.EnvType;
import org.talk.is.cheap.project.free.common.message.HttpBody;
import org.talk.is.cheap.project.free.common.message.impl.WorkerRegistryReq;
import org.talk.is.cheap.project.free.common.utils.IPUtils;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerClusterClient;
import org.talk.is.cheap.project.free.flow.starter.worker.config.CuratorConfig;
import org.talk.is.cheap.project.free.flow.starter.worker.config.properties.ZKConfigProperties;
import org.talk.is.cheap.project.free.flow.starter.worker.service.ClusterService;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;


/**
 * worker节点启动后，将自己的信息注册到zk中，并且主动连接scheduler集群，告知scheduler管理自己
 */
@Slf4j
public class WorkerRegister implements ApplicationListener<ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        CuratorFramework starterCuratorZKClient = applicationContext.getBean(CuratorConfig.STARTER_CURATOR_ZK_CLIENT_BEAN_NAME
                , CuratorFramework.class);
        ZKConfigProperties zkConfigProperties = applicationContext.getBean(CuratorConfig.ZK_CONFIG_PROPERTIES_BEAN_NAME,
                ZKConfigProperties.class);
        String envType = applicationContext.getEnvironment().getProperty("spring.application.env-type");
        String applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        String port = applicationContext.getEnvironment().getProperty("server.port");


        String workerId = EnvType.getByName(envType) == EnvType.CONTAINER ? System.getenv("CONTAINER_NAME") : IPUtils.getMainIP();
        workerId += ":" + port;
        String nodePath = null;
        try {
            nodePath = starterCuratorZKClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(Paths.get(zkConfigProperties.getZookeeper().getPath().getWorker(), applicationName).toString(),
                            (workerId).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("error when registry to zk", e);
            throw new RuntimeException(e);
        }


        ClusterService clusterService = applicationContext.getBean(ClusterService.class);
        String randomSchedulerId = clusterService.getRandomSchedulerId();
        log.info("random scheduler: {}", randomSchedulerId);

        URI uri = UriComponentsBuilder.fromHttpUrl("http://" + randomSchedulerId).build().toUri();
        SchedulerClusterClient schedulerClusterClient = applicationContext.getBean(SchedulerClusterClient.class);
        HttpBody<String> schedulerId = schedulerClusterClient.getSchedulerId(uri);
        log.info("scheduler id: {}", schedulerId);
//

        WorkerRegistryReq req = new WorkerRegistryReq();

        req.setData(WorkerRegistryReq.Data.builder().workerId(workerId).zkNodePath(nodePath).build());
        schedulerClusterClient.registryWorker(uri, req);

    }
}
