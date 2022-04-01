package org.talk.is.cheap.spring.cloud.ribbon.config;

import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PingUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dominiczhu
 * @version 1.0
 * @title RibbonConfiguration
 * @date 2022/3/31 2:58 下午
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class RibbonConfiguration {

    @Bean
    public IRule ribbonRule() {
        return new MyRule();
    }

    @Bean
    public IPing ribbonPing(){
        final PingUrl pingUrl = new MyPing();
        pingUrl.setPingAppendString("/actuator/health");
        return pingUrl;
    }

}
