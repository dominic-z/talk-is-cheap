package gateway.router;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("route-by-bean",
                        r -> r.order(4)
                                .path("/bean/backend/**")
                                .filters(f->f.rewritePath("/bean/backend/(?<segment>.*)","/backend/${segment}")) // 不要写错了，之前多搞了个空格，一直报错。。
                                .uri("http://localhost:8081/")

                )
                .build();
    }
}
