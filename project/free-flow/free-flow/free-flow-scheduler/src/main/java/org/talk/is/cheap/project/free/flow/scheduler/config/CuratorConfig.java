package org.talk.is.cheap.project.free.flow.scheduler.config;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talk.is.cheap.project.free.flow.scheduler.config.property.CuratorProperty;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * zk客户端配置
 */
@Configuration
public class CuratorConfig {


    @Bean
    public CuratorFramework curatorZKClient(@Autowired CuratorProperty curatorProperty) {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(curatorProperty.getConnectUrl())
                .sessionTimeoutMs(curatorProperty.getSessionTimeout())
                .connectionTimeoutMs(curatorProperty.getConnectionTimeout())
                // 权限认证
                .authorization(curatorProperty.getScheme(), curatorProperty.getZkAuthId().getBytes(StandardCharsets.UTF_8))
                .aclProvider(new ACLProvider() {
                    @Override
                    public List<ACL> getDefaultAcl() {
                        // 创建 Digest 认证的 ACL（所有权限）
                        try {
                            return Arrays.asList(
                                    new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(curatorProperty.getZkAuthId()))),
                                    new ACL(ZooDefs.Perms.READ, new Id("world", "anyone"))
                            );
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public List<ACL> getAclForPath(String path) {
                        // 为特定路径定制 ACL（示例：/sensitive 路径仅管理员可见）
                        // 我没配
                        return getDefaultAcl();
                    }
                })
                // 重试策略
                .retryPolicy(new ExponentialBackoffRetry(
                        curatorProperty.getCuratorRetryPolicyProperty().getBaseSleepTime(),
                        curatorProperty.getCuratorRetryPolicyProperty().getMaxRetries(),
                        curatorProperty.getCuratorRetryPolicyProperty().getMaxSleep()))
                .build();

        // 启动客户端
        curatorFramework.start();
        return curatorFramework;
    }

}

