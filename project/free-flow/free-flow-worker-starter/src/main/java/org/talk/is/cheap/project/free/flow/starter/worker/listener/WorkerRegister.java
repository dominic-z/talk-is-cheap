package org.talk.is.cheap.project.free.flow.starter.worker.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.talk.is.cheap.project.free.flow.starter.worker.service.ClusterService;


/**
 * worker节点启动后，将自己的信息注册到zk中，并且主动连接scheduler集群，告知scheduler管理自己
 */
@Slf4j
public class WorkerRegister implements ApplicationListener<ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        ClusterService clusterService = applicationContext.getBean(ClusterService.class);
        clusterService.registryWorker();
    }
}
