package org.talk.is.cheap.project.free.flow.scheduler.cluster.gateway;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.SchedulerClusterManager;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * 一个简单的网关，用于将请求引导至Scheduler集群的主节点。
 * todo: 优化选举算法，能链接上最多的worker的scheduler才能称为leader
 */
@Component
@Slf4j
public class SchedulerLeadToLeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<SchedulerLeadToLeaderGatewayFilterFactory.Config> implements ApplicationContextAware {

//    没法这么做了，会产生循环依赖，schedulerClusterManager->workerClusterManager->FeignClient->Gateway框架->schedulerClusterManager，
//    就是尴尬在Feign和Gateway框架有依赖，所以导致了循环依赖
//    所以只能通过applicationContext来处理，还好schedulerClusterManager实际用的地方是异步的，不需要考虑schedulerClusterManager有没有准备好。
//    @Autowired
//    private SchedulerClusterManager schedulerClusterManager;

    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Data
    public static class Config{

    }



    public SchedulerLeadToLeaderGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

                try {

                    SchedulerClusterManager schedulerClusterManager = applicationContext.getBean(SchedulerClusterManager.class);
                    String leader = schedulerClusterManager.getLeaderId();
                    ServerHttpRequest request = exchange.getRequest();
                    log.info("leader: {}, origin uri: {}, path: {}", leader,request.getURI(),request.getPath());

                    URI newUri = UriComponentsBuilder.newInstance()
                            .scheme(request.getURI().getScheme())
                            .host(leader.split(":")[0])
                            .port(leader.split(":")[1])
                            .path(request.getPath().toString())
                            .build()
                            .toUri();
                    Route route = (Route) exchange.getAttribute(GATEWAY_ROUTE_ATTR);

                    assert route != null;
                    Route newRoute = Route.async()
                            .asyncPredicate(route.getPredicate())
                            .id(route.getId())
                            .uri(newUri)
                            .build();

                    ServerWebExchange newServerWebExchange = exchange.mutate().build();
                    newServerWebExchange.getAttributes().put(GATEWAY_ROUTE_ATTR, newRoute);
                    return chain.filter(newServerWebExchange);
                } catch (Exception e) {
                    return Mono.error(e);
                }
            }
        };
    }
}
