package org.talk.is.cheap.spring.cloud.ribbon.config;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dominiczhu
 * @version 1.0
 * @title MyRule
 * @date 2022/3/31 3:39 下午
 */
@Slf4j
public class MyRule extends RandomRule {

    @Override
    public Server choose(Object key) {
        log.info("my choose: {}", key);
        return super.choose(key);
    }

    @Override
    public Server choose(ILoadBalancer lb, Object key) {
        log.info("lb:{} my choose: {}",lb, key);
        return super.choose(lb, key);
    }
}
