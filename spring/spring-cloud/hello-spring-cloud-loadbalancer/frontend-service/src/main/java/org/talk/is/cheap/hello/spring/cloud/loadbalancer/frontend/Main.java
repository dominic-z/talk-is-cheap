package org.talk.is.cheap.hello.spring.cloud.loadbalancer.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.talk.is.cheap.hello.spring.cloud.loadbalancer.frontend.config.MyCustomIPLoadBalancerConfig;
import org.talk.is.cheap.hello.spring.cloud.loadbalancer.frontend.config.SpringRandomLoadBalancerConfig;
import org.talk.is.cheap.hello.spring.cloud.loadbalancer.frontend.config.NacosLoadBalancerConfig;


@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
// 全局配置，对所有负载均衡客户端都生效
//@LoadBalancerClients(defaultConfiguration = SpringRandomLoadBalancerConfig.class)

// 针对某个Client特殊配置
//@LoadBalancerClients(
//        value = {@LoadBalancerClient(name = "backend-service", configuration = SpringRandomLoadBalancerConfig.class)}
//)

// 使用nacosLoadbalancer 不知道有没有生效。。。。。
// 应该是生效了，在NacosLoadBalancerConfig和NacosLoadBalancer之中断点可以看到运行
@LoadBalancerClients(defaultConfiguration = NacosLoadBalancerConfig.class)

// 自定义loadbalancer
//@LoadBalancerClients(defaultConfiguration = MyCustomIPLoadBalancerConfig.class)
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}