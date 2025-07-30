package org.talk.is.cheap.project.free.flow.scheduler.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.SchedulerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;

//@Component
@Slf4j
public class SchedulerStartedApplicationEventListener implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private SchedulerClusterManager schedulerClusterManager;

    @Autowired
    private WorkerClusterManager workerClusterManager;
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {

        log.info("scheduler application ready");
        try {
            schedulerClusterManager.registryAndElection();
        } catch (Exception e) {
            log.error("error to start scheduler election", e);
            throw new RuntimeException(e);
        }

    }
}
