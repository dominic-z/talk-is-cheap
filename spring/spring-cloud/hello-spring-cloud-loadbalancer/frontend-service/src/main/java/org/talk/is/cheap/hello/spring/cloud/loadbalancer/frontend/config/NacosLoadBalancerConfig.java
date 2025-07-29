package org.talk.is.cheap.hello.spring.cloud.loadbalancer.frontend.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.loadbalancer.DefaultLoadBalancerAlgorithm;
import com.alibaba.cloud.nacos.loadbalancer.LoadBalancerAlgorithm;
import com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancer;
import com.alibaba.cloud.nacos.loadbalancer.ServiceInstanceFilter;
import com.alibaba.cloud.nacos.util.InetIPv6Utils;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class NacosLoadBalancerConfig {

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;


    /**
     * nacos3的loadbalancer不太一样，新增了三个参数，但是loadbalancer本身源码不难，看一下源码就大体能指导多的那几个参数干啥的
     * @param environment
     * @param loadBalancerClientFactory
     * @return
     */
    @Bean
    public ReactorLoadBalancer<ServiceInstance> reactorServiceInstanceLoadBalancer(Environment environment, LoadBalancerClientFactory loadBalancerClientFactory) {

        List<ServiceInstanceFilter> filters = List.of(
                new ServiceInstanceFilter() {
                    // 对serviceInstances作一些过滤
                    @Override
                    public List<ServiceInstance> filterInstance(Request<?> request, List<ServiceInstance> serviceInstances) {
                        log.info("in filter, request: {}, context: {}",request,request.getContext());
                        // 方便打印
                        log.info("in filter, instances: {}",new Gson().toJson(serviceInstances));
                        return serviceInstances;
                    }

                    @Override
                    public int getOrder() {
                        return 0;
                    }
                }
        );

        String name = LoadBalancerClientFactory.getName(environment);


        // 核心实现，用来从多个service里面选一个
        Map<String, LoadBalancerAlgorithm> algorithmMap = new HashMap<>();
        // serviceName-> 负载均衡算法，可以特殊指定，也可以自己实现
        // 默认的DefaultLoadBalancerAlgorithm是一个权重算法，可以在nacos的服务列表-服务详情里面调整每个服务的权重，从而影响负载均衡算法
        // 还可以控制每个instance的上线与下线
        // 还可以控制每个instance的meta_data，例如在某个instance的meta data加一个字段{
        //	"preserved.register.source": "SPRING_CLOUD",
        //	"aa": "bb"
        //}
        // 那么这个aa就可以作为serviceInstance的metadata获取到
        // 从而可以控制负载均衡的算法，可以实现基于zone或者任意自定义的负载均衡
        // 不过nacos里服务修改总会有点延迟，修改完成后不会立即更新。。
        // nacos真好用阿
        algorithmMap.put(name,new DefaultLoadBalancerAlgorithm(){
            @Override
            public ServiceInstance getInstance(Request<?> request, List<ServiceInstance> serviceInstances) {
                // 简单打印点日志
                log.info("in algorithm, request: {}, context: {}",request,request.getContext());
                log.info("in algorithm, instance: {}", new Gson().toJson(serviceInstances));
                return super.getInstance(request, serviceInstances);
            }
        });
        // 一个默认的，如果没有找到对应的算法，则使用这个默认的，这个key在NacosLoadBalancer会被用到
        algorithmMap.put(LoadBalancerAlgorithm.DEFAULT_SERVICE_ID,new DefaultLoadBalancerAlgorithm());

        return new NacosLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
                name,
                nacosDiscoveryProperties,
                new InetIPv6Utils(new InetUtilsProperties()),
                filters,
                algorithmMap
                );
    }

}
