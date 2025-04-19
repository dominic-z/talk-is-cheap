package org.talk.is.cheap.hippo4j.config;

import cn.hippo4j.core.executor.DynamicThreadPool;
import cn.hippo4j.core.executor.support.ThreadPoolBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorPoolConfig {

    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(4, 5, 1000, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<>(10),
                new ThreadPoolExecutor.AbortPolicy());

    }

    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor messageConsumeDynamicExecutor() {
        String threadPoolId = "zzz-message-consume";
        ThreadPoolExecutor messageConsumeDynamicExecutor = ThreadPoolBuilder.builder()
                .threadFactory(threadPoolId)
                .threadPoolId(threadPoolId)
                // 配置默认的线程池配置，因为初始情况下，并不会拉hippo-server的配置，而是先使用本地配置，只有当hippo-server发生update的时候，才会同步到本地。
                .workQueue(new ArrayBlockingQueue<>(8))
                .maximumPoolSize(8)
                .corePoolSize(4)
                // 默认是AbortPolicy，队列满了会抛出异常导致线程池关闭
                .rejected(new ThreadPoolExecutor.DiscardPolicy())
                .dynamicPool()
                .build();
        return messageConsumeDynamicExecutor;
    }

    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor messageProduceDynamicExecutor() {
        String threadPoolId = "zzz-message-produce";
        ThreadPoolExecutor messageProduceDynamicExecutor = ThreadPoolBuilder.builder()
                .threadFactory(threadPoolId)
                .threadPoolId(threadPoolId)
                .workQueue(new ArrayBlockingQueue<>(8))
                .maximumPoolSize(8)
                .corePoolSize(4)
                .dynamicPool()
                .build();
        return messageProduceDynamicExecutor;
    }
}
