package org.talk.is.cheap.project.free.flow.starter.worker.config;

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
import org.talk.is.cheap.project.free.flow.starter.worker.config.properties.ZKConfigProperties;
import org.talk.is.cheap.project.free.flow.common.utils.YamlUtils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class CuratorConfig {

    private static final String ZK_CONFIG_FILENAME = "free-flow-worker-zookeeper.yaml";

    public static final String STARTER_CURATOR_ZK_CLIENT_BEAN_NAME = "starterCuratorZKClient";
    public static final String ZK_CONFIG_PROPERTIES_BEAN_NAME = "zkConfigProperties";

    @Bean(name = ZK_CONFIG_PROPERTIES_BEAN_NAME)
    public ZKConfigProperties zkConfigProperties() {
        return YamlUtils.load(CuratorConfig.class.getClassLoader().getResource(ZK_CONFIG_FILENAME), ZKConfigProperties.class);
    }

    /**
     * @param zkConfigProperties
     * @return
     */
    @Bean(name = STARTER_CURATOR_ZK_CLIENT_BEAN_NAME)
    public CuratorFramework starterCuratorZKClient(@Autowired ZKConfigProperties zkConfigProperties) {
        CuratorFramework curatorZKClient = CuratorFrameworkFactory.builder()
                .connectString(zkConfigProperties.getZookeeper().getConnectUrl())
                .sessionTimeoutMs(zkConfigProperties.getZookeeper().getSessionTimeout())
                .connectionTimeoutMs(zkConfigProperties.getZookeeper().getConnectionTimeout())
                // 权限认证
                .authorization(zkConfigProperties.getZookeeper().getScheme(),
                        zkConfigProperties.getZookeeper().getZkAuthId().getBytes(StandardCharsets.UTF_8))
                .aclProvider(new ACLProvider() {
                    @Override
                    public List<ACL> getDefaultAcl() {
                        // 创建 Digest 认证的 ACL（所有权限）
                        try {
                            return Arrays.asList(
                                    new ACL(ZooDefs.Perms.ALL, new Id("digest",
                                            DigestAuthenticationProvider.generateDigest(zkConfigProperties.getZookeeper().getZkAuthId()))),
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
                        zkConfigProperties.getRetryPolicy().getBaseSleepTime(),
                        zkConfigProperties.getRetryPolicy().getMaxRetries(),
                        zkConfigProperties.getRetryPolicy().getMaxSleep()))
                .build();

        // 启动客户端
        curatorZKClient.start();
        return curatorZKClient;
    }
}
