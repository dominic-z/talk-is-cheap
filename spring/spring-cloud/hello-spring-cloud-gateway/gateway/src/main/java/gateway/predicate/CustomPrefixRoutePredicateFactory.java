package gateway.predicate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

/**
 * 自定义的predicate，抄的https://cloud.tencent.com/developer/article/1929827
 * 但实际上和自定义filter差不多，名字很重要，配置会根据bena的name来寻找对应的factory
 */
@Component
@Slf4j
public class CustomPrefixRoutePredicateFactory extends AbstractRoutePredicateFactory<CustomPrefixRoutePredicateFactory.CustomPredicateFactoryConfig> {


    @Data
    public static class CustomPredicateFactoryConfig{

        private String prefix;
    }

    public CustomPrefixRoutePredicateFactory() {
        super(CustomPredicateFactoryConfig.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(CustomPredicateFactoryConfig config) {
        return serverWebExchange -> {
            RequestPath path = serverWebExchange.getRequest().getPath();
            if(StringUtils.isNotBlank(config.getPrefix()) && StringUtils.startsWith(path.toString(),config.getPrefix())){
                log.info("custom predicate: activated");
                return true;
            }
            log.info("custom predicate: not activated");
            return false;
        };
    }
}
