package geektime.spring.data.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author dominiczhu
 * @version 1.0
 * @title JDBCProperties
 * @date 2021/9/14 上午11:05
 */
@ConfigurationProperties(prefix = "spring.jdbc.datasource.druid.dmp")
@Data
public class DmpJDBCProperties {

    private String url;
    private String driverClassName;
    private String username;
    private String password;

}
