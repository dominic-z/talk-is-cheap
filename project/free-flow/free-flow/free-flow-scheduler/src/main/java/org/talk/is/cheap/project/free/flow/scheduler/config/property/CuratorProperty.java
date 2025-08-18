package org.talk.is.cheap.project.free.flow.scheduler.config.property;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "apache.zookeeper")
@Data
public class CuratorProperty {
    // 服务器连接地址，集群模式则使用逗号分隔如：ip1:host,ip2:host
    private String connectUrl;

    // 会话超时时间：单位ms
    private Integer sessionTimeout;

    // 连接超时时间：单位ms
    private Integer connectionTimeout;

    // ACL权限控制，验证策略
    private String scheme;

    // 验证内容id
    private String zkAuthId;

    @Autowired
    private CuratorRetryPolicyProperty curatorRetryPolicyProperty;
}
