package demo.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author dominiczhu
 * @version 1.0
 * @title AopConfig
 * @date 2021/9/14 下午5:14
 */
@Configuration
@ComponentScan("demo")
@EnableAspectJAutoProxy
public class AppConfig {
}
