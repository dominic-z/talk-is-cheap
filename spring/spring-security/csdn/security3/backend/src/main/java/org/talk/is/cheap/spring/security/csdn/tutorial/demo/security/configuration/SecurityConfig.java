package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.filter.JwtAuthenticationTokenFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 将 AuthenticationManager 对象添加到spring容器中.（添加到我们创建的 SecurityConfig 配置类中）
     * @param authConfig
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                // 开启授权保护
//                .authorizeHttpRequests(authorize -> authorize
//                        // 不需要认证的地址有哪些
//                        .requestMatchers("/blog/**", "/public/**", "/about").permitAll()	// ** 通配符
//                        // 对所有请求开启授权保护
//                        .anyRequest().
//                        // 已认证的请求会被自动授权
//                                authenticated()
//                )
//                // 使用默认的登陆登出页面进行授权登陆
//                .formLogin(Customizer.withDefaults())
//                // 启用“记住我”功能的。允许用户在关闭浏览器后，仍然保持登录状态，直到他们主动注销或超出设定的过期时间。
//                .rememberMe(Customizer.withDefaults());
        // 关闭 csrf CSRF（跨站请求伪造）是一种网络攻击，攻击者通过欺骗已登录用户，诱使他们在不知情的情况下向受信任的网站发送请求。
        http.csrf(AbstractHttpConfigurer::disable) // 基于token，不需要csrf
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 基于token，不需要session
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/login/**",  "/getPicCheckCode","/error").permitAll() // 放行api
                        .requestMatchers("/business/hello1").hasAnyRole("ADMIN","USER") // 放行api
                        .requestMatchers("/business/hello2").hasRole("ADMIN") // 放行api
                        .requestMatchers("/business/hello3").hasAuthority("AUTH1") // 放行api
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()  // 跨域的option请求放行
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

}
