package org.talk.is.cheap.project.free.flow.scheduler.cluster.gateway;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
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
public class SchedulerLeadToLeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<SchedulerLeadToLeaderGatewayFilterFactory.Config> {

    @Data
    public static class Config{

    }

    @Autowired
    private SchedulerClusterManager schedulerClusterManager;

    public SchedulerLeadToLeaderGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

                try {

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
