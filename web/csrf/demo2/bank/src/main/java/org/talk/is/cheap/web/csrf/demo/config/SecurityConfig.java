package org.talk.is.cheap.web.csrf.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(c -> c.requestMatchers("/**").authenticated())
                .formLogin(Customizer.withDefaults())
//                .antMatcher("/**").authorizeRequests(authorize -> authorize.anyRequest().authenticated()).formLogin()
               .csrf(c -> c.disable())
                .build();
    }
}
