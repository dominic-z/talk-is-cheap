package org.talk.is.cheap.project.free.flow.starter.worker.config;


import org.springframework.context.annotation.Bean;
import org.talk.is.cheap.project.free.flow.starter.worker.contoller.impl.ClusterControllerImpl;
import org.talk.is.cheap.project.free.flow.starter.worker.listener.WorkerRegister;
import org.talk.is.cheap.project.free.flow.starter.worker.service.ClusterService;

public class ClusterConfig {

    @Bean
    public ClusterControllerImpl clusterController() {
        return new ClusterControllerImpl();
    }

    @Bean
    public ClusterService clusterService() {
        return new ClusterService();
    }

}
