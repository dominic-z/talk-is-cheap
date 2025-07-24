package gateway.route;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;


/**
 *
 * 动态路由，没有通过配置，直接通过代码控制增减的路由。来自https://zhuanlan.zhihu.com/p/633766299
 * 按顺序请求
 * 1. http://localhost:8080/gateway/dynamic-route/hi---失败
 * 2. http://localhost:8080/gateway/add-dynamic-route
 * {
 *     "code":1,
 *     "data":8081,
 *     "message":"hihimsg"
 * }
 *
 * 3. 请求http://localhost:8080/gateway/dynamic-route/hi  成功
 * 4. http://localhost:8080/gateway/delete-dynamic-route 删除路由
 * 5. http://localhost:8080/gateway/dynamic-route/hi---失败
 */
@Component
@Slf4j
public class DynamicRouter implements ApplicationEventPublisherAware {

    /**
     * 路由信息写入器
     */
    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    private static final String ROUTE_ID = "route-10";

    public void addRouteConfig(int port) {
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(ROUTE_ID);
//        path的predicate
        PredicateDefinition predicateDefinition = new PredicateDefinition();
        predicateDefinition.setName("Path");
        predicateDefinition.setArgs(Map.of("patterns", "/gateway/dynamic-route/**"));
        routeDefinition.setPredicates(List.of(predicateDefinition));

        FilterDefinition filterDefinition = new FilterDefinition();
        filterDefinition.setName("RewritePath");
        filterDefinition.setArgs(Map.of("regexp", "/gateway/dynamic-route/(?<segment>.*)",
                "replacement", "/backend/${segment}"));

        routeDefinition.setFilters(List.of(filterDefinition));

        UriComponents uriComponents = UriComponentsBuilder.fromUriString("http://localhost:"+port).build();
        routeDefinition.setUri(uriComponents.toUri());
        log.info("add route, {}",routeDefinition);

//        在 Reactor 响应式编程中，Mono 的 subscribe() 方法是触发数据流执行的核心操作，它表示 “订阅” 这个响应式序列，只有调用了 subscribe()，Mono 中定义的逻辑才会实际执行（这体现了响应式编程的 “懒执行” 特性）。
        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();

//        发布刷新路由的信息
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    public void deleteRouteConfig(){
        log.info("delete route");

        routeDefinitionWriter.delete(Mono.just(ROUTE_ID)).subscribe();
        publisher.publishEvent(new RefreshRoutesEvent(this));

    }

}
