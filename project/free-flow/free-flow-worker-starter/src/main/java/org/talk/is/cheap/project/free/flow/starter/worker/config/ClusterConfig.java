package org.talk.is.cheap.project.free.flow.starter.worker.config;


import feign.codec.Decoder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.talk.is.cheap.project.free.flow.starter.worker.contoller.impl.ClusterControllerImpl;
import org.talk.is.cheap.project.free.flow.starter.worker.listener.WorkerRegister;
import org.talk.is.cheap.project.free.flow.starter.worker.service.ClusterService;

import java.util.stream.Collectors;

public class ClusterConfig {

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
