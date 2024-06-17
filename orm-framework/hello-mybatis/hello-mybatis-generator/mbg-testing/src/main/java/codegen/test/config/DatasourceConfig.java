package codegen.test.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {

    @ConfigurationProperties(prefix = "spring.jdbc.datasource.druid")
    @Bean
    public DataSource datasource(){
        return DruidDataSourceBuilder.create().build();
    }

}
