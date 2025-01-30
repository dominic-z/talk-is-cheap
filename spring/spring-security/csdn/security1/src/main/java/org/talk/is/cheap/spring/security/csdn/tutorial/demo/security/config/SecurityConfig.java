package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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


/**
 * WebSecurityConfigurerAdapter是Spring Security的配置体系中的一个重要类，但是这个类将在5.7版本被@Deprecated所标记，未来这个类将被移除123。
 * 在新版的Spring Security里面，我们只需要在自定义的配置类头顶添加一个@EnableWebSecurity注解即可将你写的这个注入到IOC容器中4
 * <p>
 * WebSecurityConfigurerAdapter 是Spring Security中用于自定义HTTP安全配置的一个类。开发者可以通过继承这个类来定制端点授权或Authentication
 * Manager配置。然而，从Spring Security 5.7.0版本开始，WebSecurityConfigurerAdapter 已被标记为过时，并且Spring推荐使用基于组件的安全配置方法。
 * <p>
 * 新的安全配置方法
 * <p>
 * 在新的安全配置方法中，不再需要继承 WebSecurityConfigurerAdapter。相反，可以通过定义一些Bean来配置安全性。例如，可以创建一个 SecurityFilterChain
 * Bean来配置HTTP安全性，或者使用 WebSecurityCustomizer 来配置Web安全性。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {


    /**
     * 这玩意是一个密码加密器，相当于存储在数据库里的密码是加密的，而前端传回来的密码是原文的时候，就可以做比对
     * 实际密码比对调用的就是这个方法的matches方法。
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder(){


        return new BCryptPasswordEncoder();
    }
    @Autowired
    JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

//        看不懂要干啥
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return
                http.csrf(AbstractHttpConfigurer::disable)
                        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                        下面这些不会被拦截，还要包括js
                        .authorizeHttpRequests(req -> req.requestMatchers("/api/user/login","/api/hello","/api/error",
                                "/*.html","/js/*").anonymous().anyRequest().authenticated())
                        .build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}