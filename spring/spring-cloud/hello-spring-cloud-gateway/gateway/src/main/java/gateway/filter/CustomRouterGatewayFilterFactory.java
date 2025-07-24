package gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Random;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * 在filter里动态地修改路由，从而实现动态路由
 */
@Component
@Slf4j
public class CustomRouterGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomRouterGatewayFilterFactory.Config> {

    public CustomRouterGatewayFilterFactory() {
        super(Config.class);
    }

    private static final Random RANDOM = new Random();

    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

                // 这些request之的都是经过了前面的filter的（例如rewritepath）之后的request对象。filter并不会直接修改请求的目标uri，而是在所有的filter都过了之后才更新目标path的
                URI uri = exchange.getRequest().getURI();
                RequestPath path = exchange.getRequest().getPath();
                log.info("config prob: {}, origin uri: {}, path: {}", config.getProb(), uri, path);
                URI newUri = UriComponentsBuilder.newInstance()
                        .scheme(uri.getScheme())
                        .host("localhost")
//                        需要启动两个后端服务
                        .port((config.getProb() == null || RANDOM.nextDouble() <= config.getProb()) ? 8081 : 8082)
                        .path(path.toString())
                        .build()
                        .toUri();
                Route route = (Route) exchange.getAttribute(GATEWAY_ROUTE_ATTR);

                log.info("new uri: {}", newUri);
                assert route != null;
                Route newRoute = Route.async()
                        .asyncPredicate(route.getPredicate())
                        .id(route.getId())
//                        .filters(route.getFilters())
//                        .order(route.getOrder())
                        .uri(newUri)
                        .build();

                ServerWebExchange newServerWebExchange = exchange.mutate().build();
                newServerWebExchange.getAttributes().put(GATEWAY_ROUTE_ATTR, newRoute);

                return chain.filter(newServerWebExchange);
            }
        };
    }

    @Data
    public static class Config {
        Double prob;
    }
}
