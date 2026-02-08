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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.talk.is.cheap.project.free.flow.starter.worker.config.properties.ZKConfigProperties;
import org.talk.is.cheap.project.free.flow.common.utils.YamlUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CuratorAutoConfig {

    private static final String ZK_CONFIG_FILENAME = "free-flow-worker-zookeeper.yaml";

    public static final String STARTER_CURATOR_ZK_CLIENT_BEAN_NAME = "starterCuratorZKClient";
    public static final String ZK_CONFIG_PROPERTIES_BEAN_NAME = "zkConfigProperties";

    // 定制一个名字，不要影响实际应用的bean
    @Bean(name = ZK_CONFIG_PROPERTIES_BEAN_NAME)
    public ZKConfigProperties zkConfigProperties() throws IOException {
        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        Resource resource = patternResolver.getResource("classpath:/" + ZK_CONFIG_FILENAME);
        if (!resource.exists()) {
            throw new FileNotFoundException(ZK_CONFIG_FILENAME + " not found");
        }
        return YamlUtil.loadFile(resource.getURL(), ZKConfigProperties.class);
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
