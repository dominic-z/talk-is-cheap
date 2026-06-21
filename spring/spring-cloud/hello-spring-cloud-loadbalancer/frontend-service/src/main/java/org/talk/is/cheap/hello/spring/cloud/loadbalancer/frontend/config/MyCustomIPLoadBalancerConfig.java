package org.talk.is.cheap.hello.spring.cloud.loadbalancer.frontend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.talk.is.cheap.hello.spring.cloud.loadbalancer.frontend.config.custom.loadbalancer.MyCustomIPLoadBalancer;

@Slf4j
public class MyCustomIPLoadBalancerConfig {

    @Bean
    public ReactorLoadBalancer<ServiceInstance> reactorServiceInstanceLoadBalancer(Environment environment, LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = LoadBalancerClientFactory.getName(environment);
        log.info("service name: {}", name);
        return new MyCustomIPLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
    }
}
