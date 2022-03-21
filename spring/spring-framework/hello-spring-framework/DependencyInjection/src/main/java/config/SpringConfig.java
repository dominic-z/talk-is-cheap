package config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author dominiczhu
 * @version 1.0
 * @title SpringConfig
 * @date 2021/3/23 下午7:54
 */
@Configuration
@ComponentScan(basePackages = {"autowired","services"})
public class SpringConfig {
}
