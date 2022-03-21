package org.example.spring.starter.client.env;

import org.example.spring.starter.log.Loggers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ClientEnvPostProcessor
 * @date 2022/2/15 10:37 上午
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClientEnvPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        if (environment.getPropertySources().contains("bootstrap")) {
            return;
        }

        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        try {
            List<PropertySource<?>> sources = loader.load("hello-starter-client", new ClassPathResource("client.yml"));
            for (PropertySource<?> source : sources) {
                environment.getPropertySources().addFirst(source);
            }
        } catch (IOException e) {
            Loggers.ERROR_LOG.error("error when parse client.yml in hello-spring-boot-starter-feign-client", e);
            throw new RuntimeException(e);
        }


    }
}
