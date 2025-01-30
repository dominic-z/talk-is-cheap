package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.filter.JwtAuthenticationFilter;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.pw.CustomMd5PasswordEncoder;

import java.util.Collections;

@Configuration
@EnableWebSecurity
//这个注解是针对方法进行安全校验，对应的根据权限判断当前用户能不能访问某个接口，需要在接口上设置权限
//例如@PreAuthorize("hasAuthority('bnt.sysRole.list')")就会允许拥有'bnt.sysRole.list'授权的用户访问
@EnableMethodSecurity

public class SecurityConfig {

    /**
     * 密码明文加密方式配置
     *
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new CustomMd5PasswordEncoder();
    }

    @Autowired
    JwtAuthenticationFilter jwtAuthenticationTokenFilter;
    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    /**
     * 获取AuthenticationManager（认证管理器），登录时认证使用
     *
     * @param authenticationConfiguration
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        下面这段在教程里没有体现，是从另一个教程里扣过来的，如果不加这个，security还是会继续使用自己的过滤器，而不会使用我们设置的过滤器进行校验
//        如果不加这个，security不会判断登录状态信息
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(c -> c.authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler));

        // 新写法
        return http
                // 基于 token，不需要 csrf
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(r ->
                                // 指定某些接口不需要通过验证即可访问。登录接口肯定是不需要认证的
                                r.requestMatchers("/admin/system/index/login").permitAll()
                                        // 静态资源和swagger接口，可匿名访问
                                        .requestMatchers(HttpMethod.GET, "/", "/*.html", "/**.html", "/**.css", "/**.js"
                                                , "/profile/**", "/error").permitAll()
                                        .requestMatchers("/swagger-ui.html", "/swagger-resources/**", "/webjars/**",
                                                "/*/api-docs", "/druid/**", "/doc.html").permitAll()
//                                spring3以后通配符的使用方式有点变化，不再接收/**/*.css这种写法，否则会抛出No more pattern data allowed after {*.
//                                ..} or ** pattern element异常
//                                根据网上的说法进行修改
//                                .requestMatchers(HttpMethod.GET, "/", "/*.html", "/**/*.html", "/**/*.css", "/**/*.js"
//                                        , "/profile/**").permitAll()
//                                .requestMatchers("/swagger-ui.html", "/swagger-resources/**", "/webjars/**",
//                                "/*/api" +
//                                        "-docs", "/druid/**", "/doc.html").permitAll()
                                        // 这里意思是其它所有接口需要认证才能访问
                                        .anyRequest().authenticated()
                )
//下面这几段没看懂
                // 基于 token，不需要 session
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 开启跨域以便前端调用接口
                // cors security 解决方案
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .build();
    }

    /**
     * 配置跨源访问(CORS)
     *
     * @return
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
