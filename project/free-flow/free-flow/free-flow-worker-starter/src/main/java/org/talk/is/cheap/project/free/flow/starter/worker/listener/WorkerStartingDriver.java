package org.talk.is.cheap.project.free.flow.starter.worker.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.talk.is.cheap.project.free.flow.common.exception.IllegalTaskDefinitionException;
import org.talk.is.cheap.project.free.flow.starter.worker.cluster.service.ClusterService;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service.LocalTaskDefinitionService;


/**
 * worker节点启动后，将自己的信息注册到zk中，并且主动连接scheduler集群，告知scheduler管理自己
 */
@Slf4j
public class WorkerStartingDriver implements ApplicationListener<ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // 没法通过注入的方式获取
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        ClusterService clusterService = applicationContext.getBean(ClusterService.class);
        clusterService.listenAndSetSchedulerLeader();

        LocalTaskDefinitionService localTaskDefinitionService = applicationContext.getBean(LocalTaskDefinitionService.class);
        try {
            localTaskDefinitionService.prepareAndValidateTaskDefinition();
        } catch (IllegalTaskDefinitionException e) {
            throw new RuntimeException(e);
        }

        // 以上任务都完成，注册到zk中
        clusterService.registryToZK();
    }
}
