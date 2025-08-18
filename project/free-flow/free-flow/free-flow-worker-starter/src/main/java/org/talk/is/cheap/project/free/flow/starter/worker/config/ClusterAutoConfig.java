package org.talk.is.cheap.project.free.flow.starter.worker.config;


import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerClusterClient;
import org.talk.is.cheap.project.free.flow.starter.worker.cluster.contoller.impl.ClusterControllerImpl;
import org.talk.is.cheap.project.free.flow.starter.worker.cluster.service.ClusterService;

import java.util.stream.Collectors;

@Configuration
@EnableFeignClients(clients = {SchedulerClusterClient.class})
public class ClusterAutoConfig {

    @Bean
    public ClusterControllerImpl clusterController() {
        return new ClusterControllerImpl();
    }

    @Bean
    public ClusterService clusterService() {
        return new ClusterService();
    }

    /**
     * 在webflux中使用feign，必须得手动创建一个converter，否则报错
     * @param converters
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }
}
