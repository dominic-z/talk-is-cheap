package com.example.springboot.hellospringboot.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author dominiczhu
 * @version 1.0
 * @title MysqlDatasourceConfig
 * @date 2021/9/14 下午8:12
 */
@Configuration
public class MysqlDatasourceConfig {


    @ConfigurationProperties(prefix = "spring.jdbc.datasource.druid.yiibai")
    @Bean("yiibaiDatasource")
    public DataSource yiibaiDatasource(){
        return DruidDataSourceBuilder.create().build();
    }



}
