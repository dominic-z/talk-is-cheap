package gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


/**
 * 身份校验
 */
@Component
@Slf4j
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.AuthGatewayFilterFactoryConfig> {

    @Data
    public static class AuthGatewayFilterFactoryConfig{

        private Boolean needAuth;
    }


    public static class AuthGatewayFilter implements GatewayFilter {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            return null;
        }
    }

    public AuthGatewayFilterFactory() {
        super(AuthGatewayFilterFactoryConfig.class);
    }

    @Override
    public GatewayFilter apply(AuthGatewayFilterFactoryConfig config) {

        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                if(config.getNeedAuth()){
//            直接抛出异常，不写了
//                    注意这个是ServerHttpResponse。
                    log.info("need auth");
                    ServerHttpResponse response = exchange.getResponse();
                    DataBuffer dataBuffer = response.bufferFactory().wrap("认证不通过".getBytes(StandardCharsets.UTF_8));
                    Mono<Void> voidMono = response.writeWith(Mono.just(dataBuffer));
                    return voidMono;
                }else{
                    log.info("do not auth");
                    chain.filter(exchange);
                    return Mono.empty();
                }
            }
        };
    }
}
