package org.talk.is.cheap.project.free.flow.starter.worker.config;


import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.talk.is.cheap.project.free.flow.starter.worker.contoller.impl.ClusterControllerImpl;
import org.talk.is.cheap.project.free.flow.starter.worker.service.ClusterService;

import java.util.stream.Collectors;

public class ClusterAutoConfig {

    @Bean
    public ClusterControllerImpl clusterController() {
        return new ClusterControllerImpl();
    }

    @Bean
    public ClusterService clusterService() {
        return new ClusterService();
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }
}
