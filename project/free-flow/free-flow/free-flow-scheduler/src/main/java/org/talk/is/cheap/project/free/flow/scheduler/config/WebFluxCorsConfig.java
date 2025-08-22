package org.talk.is.cheap.project.free.flow.scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;


// https://www.doubao.com/thread/w74e3a14fe66ff4ac
// 开发过程用，开启跨域
@Configuration
public class WebFluxCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        // 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();
        // 允许的来源（生产环境建议指定具体域名）
        config.addAllowedOriginPattern("*");
        // 允许的HTTP方法
        config.addAllowedMethod("*");
        // 允许的请求头
        config.addAllowedHeader("*");
        // 允许携带Cookie
        config.setAllowCredentials(true);
        // 暴露的响应头（前端需要获取的额外头信息）
        config.addExposedHeader("Authorization");
        // 预检请求的缓存时间（秒）
        config.setMaxAge(3600L);

        // 创建基于URL的CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        // 对所有路径应用上述CORS配置
        source.registerCorsConfiguration("/**", config);

        // 返回CORS过滤器
        return new CorsWebFilter(source);
    }
}
