package geektime.spring.data.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author dominiczhu
 * @version 1.0
 * @title DatasourceConfiguration
 * @date 2021/9/14 上午11:05
 */
@EnableConfigurationProperties(DmpJDBCProperties.class)
@Configuration
public class DatasourceConfig {

    @Autowired
    private DmpJDBCProperties jdbcProperties;

    @ConfigurationProperties(prefix = "spring.jdbc.datasource.druid.yiibai")
    @Bean
    public DataSource yiibaiDatasource(){
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    public DataSource dmpDatasource(){
        DruidDataSource druidDataSource = DruidDataSourceBuilder.create().build();
        druidDataSource.setDriverClassName(jdbcProperties.getDriverClassName());
        druidDataSource.setUrl(jdbcProperties.getUrl());
        druidDataSource.setUsername(jdbcProperties.getUsername());
        druidDataSource.setPassword(jdbcProperties.getPassword());
        return druidDataSource;
    }

}
