package gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class UserKeyResolver implements KeyResolver {
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        final String user = exchange.getRequest().getQueryParams().getFirst("user");

        if(StringUtils.isBlank(user)){
            return Mono.just("defaultUser");
        }else{
            return Mono.just(user);
        }
    }
}
