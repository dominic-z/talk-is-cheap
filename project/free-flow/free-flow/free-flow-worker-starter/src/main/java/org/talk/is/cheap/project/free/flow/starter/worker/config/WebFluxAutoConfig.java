package org.talk.is.cheap.project.free.flow.starter.worker.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.talk.is.cheap.project.free.flow.starter.worker.config.filters.HttpLoggingFilter;

@Configuration
@Import(HttpLoggingFilter.class)
public class WebFluxAutoConfig {
}
