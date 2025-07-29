package org.talk.is.cheap.hello.spring.cloud.loadbalancer.frontend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;


/**
 * 这些config本身都不是bean，不需要被spring管理，这些ReactorLoadBalancer对象只有在真正执行远程调用的时候才会被创建出来。
 */
@Slf4j
public class SpringRandomLoadBalancerConfig {
    @Bean
    public ReactorLoadBalancer<ServiceInstance> reactorServiceInstanceLoadBalancer(Environment environment, LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = environment.getProperty("loadbalancer.client.name");
        log.info("service name: {}", name);
        // loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class)
        // 关键就是这个，这个是获取某个service的所有serviceInstance
        return new RandomLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
    }

}
