package org.example.spring.starter.log.listener;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;

/**
 * @author dominiczhu
 * @version 1.0
 * @title LogbackConfigListener
 * @date 2022/2/13 4:50 下午
 */
// 一定要最后加载，否则如果app项目里也有一份logback文件，那么此配置会被覆盖掉
@Order(Ordered.LOWEST_PRECEDENCE)
public class LogbackConfigListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        // 20250406 挪到LoggerAppRunListener那去了
    }
}
