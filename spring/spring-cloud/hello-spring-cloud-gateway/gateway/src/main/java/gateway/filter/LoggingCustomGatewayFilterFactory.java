package gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingCustomGatewayFilterFactory extends AbstractGatewayFilterFactory<LoggingCustomGatewayFilterFactory.LoggingCustomFilterConfig> {

    //    必须有这个，否则默认的父类构造方法是super((Class<C>) Object.class);类型转换会出错的
    public LoggingCustomGatewayFilterFactory() {
        super(LoggingCustomFilterConfig.class);
    }

    @Override
    public GatewayFilter apply(LoggingCustomFilterConfig config) {
        log.info("logging level: {}", config.getLoggingLevel());
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                if (StringUtils.equals(config.getLoggingLevel(), "INFO")) {
                    ServerHttpRequest serverHttpRequest = exchange.getRequest();
                    log.info("custom filter before, req path: {}", serverHttpRequest.getPath());
                }
                return chain.filter(exchange).then(Mono.create(voidMonoSink -> {
                    if (StringUtils.equals(config.getLoggingLevel(), "INFO")) {
                        log.info("custom filter after, resp status: {}", exchange.getResponse().getStatusCode());
                    }
//            成功
                    voidMonoSink.success();
                }));
            }
        };
    }

    /**
     * 这个东西的作用其实就是读取配置文件中的args的参数
     */
    @Data
    public static class LoggingCustomFilterConfig {

        private String loggingLevel;
    }

}
