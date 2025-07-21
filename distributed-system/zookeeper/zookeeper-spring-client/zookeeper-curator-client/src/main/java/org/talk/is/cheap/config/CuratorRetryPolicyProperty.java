package org.talk.is.cheap.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "apache.retry-policy")
@Configuration
@Data
public class CuratorRetryPolicyProperty {
    // 初始化间隔时间
    private Integer baseSleepTime;

    // 最大重试次数
    private Integer maxRetries;

    // 最大重试间隔时间
    private Integer maxSleep;
}
