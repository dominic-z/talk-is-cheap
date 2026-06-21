package org.talk.is.cheap.config;


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

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * zk客户端配置
 */
@Configuration
public class CuratorConfig {


    /**
     * 集群管理员的客户端
     * @param curatorProperty
     * @return
     */
    @Bean
    public CuratorFramework adminCuratorClient(@Autowired CuratorProperty curatorProperty) {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(curatorProperty.getConnectUrl())
                .sessionTimeoutMs(curatorProperty.getSessionTimeout())
                .connectionTimeoutMs(curatorProperty.getConnectionTimeout())
                // 权限认证
                .authorization(curatorProperty.getScheme(), curatorProperty.getAdminAuthId().getBytes(StandardCharsets.UTF_8))
//                参考https://www.doubao.com/thread/w96d974309aab3e4e、https://www.doubao.com/thread/w1ee519f5c94d10df
//                这个东西是用于当使用这个客户端对zk进行操作的时候，会自动使用ACLProvider来为路径赋予acl权限，这样就不用每次crud的时候手动配置一个acl对象了
                .aclProvider(new ACLProvider() {
                    @Override
                    public List<ACL> getDefaultAcl() {
                        // 创建 Digest 认证的 ACL（所有权限）

                        try {
                            return Arrays.asList(
//                                    这个digest不要错了，否则也会有奇奇怪怪的auth问题
                                    new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(curatorProperty.getAdminAuthId()))),
                                    new ACL(ZooDefs.Perms.READ, new Id("world", "anyone")
                                    )
                            );
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public List<ACL> getAclForPath(String path) {
                        // 为特定路径定制 ACL（示例：/sensitive 路径仅管理员可见），例如当路径满足什么规则的饿时候，返回什么样的acl
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


    /**
     * 租户1的客户端
     * @param curatorProperty
     * @return
     */
    @Bean
    public CuratorFramework tenant1CuratorClient(@Autowired CuratorProperty curatorProperty) {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(curatorProperty.getConnectUrl())
                .sessionTimeoutMs(curatorProperty.getSessionTimeout())
                .connectionTimeoutMs(curatorProperty.getConnectionTimeout())
                // 权限认证
                .authorization(curatorProperty.getScheme(), curatorProperty.getTenant1AuthId().getBytes(StandardCharsets.UTF_8))
//                参考https://www.doubao.com/thread/w96d974309aab3e4e、https://www.doubao.com/thread/w1ee519f5c94d10df
//                这个东西是用于当使用这个客户端对zk进行操作的时候，会自动使用ACLProvider来为路径赋予acl权限，这样就不用每次crud的时候手动配置一个acl对象了
                .aclProvider(new ACLProvider() {
                    @Override
                    public List<ACL> getDefaultAcl() {
                        // 创建 Digest 认证的 ACL（所有权限）

                        try {
                            return Arrays.asList(
                                    new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(curatorProperty.getTenant1AuthId()))),
                                    new ACL(ZooDefs.Perms.READ, new Id("world", "anyone")
                                    )
                            );
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public List<ACL> getAclForPath(String path) {
                        // 为特定路径定制 ACL（示例：/sensitive 路径仅管理员可见），例如当路径满足什么规则的饿时候，返回什么样的acl
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

