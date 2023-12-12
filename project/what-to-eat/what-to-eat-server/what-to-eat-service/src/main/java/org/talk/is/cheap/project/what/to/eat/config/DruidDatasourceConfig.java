package org.talk.is.cheap.project.what.to.eat.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DruidDatasourceConfig {

//    public static final int MAX_PAGE_SIZE = 500;

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource druidDatasource() {
        return DruidDataSourceBuilder.create().build();
    }

}
