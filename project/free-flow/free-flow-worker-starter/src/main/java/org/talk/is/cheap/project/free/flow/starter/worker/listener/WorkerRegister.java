package org.talk.is.cheap.project.free.flow.starter.worker.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.talk.is.cheap.project.free.flow.starter.worker.service.ClusterService;


@Slf4j
public class WorkerRegister implements ApplicationListener<ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        ClusterService clusterService = event.getApplicationContext().getBean(ClusterService.class);
        log.info("random scheduler: {}", clusterService.getRandomScheduler());
    }
}
