package gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@Slf4j
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        log.info("global filter before, req path: {}", serverHttpRequest.getPath());

        return chain.filter(exchange).then(Mono.create(voidMonoSink -> {
            log.info("global filter after, resp status: {}",exchange.getResponse().getStatusCode());
//            成功
            voidMonoSink.success();
        }));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
