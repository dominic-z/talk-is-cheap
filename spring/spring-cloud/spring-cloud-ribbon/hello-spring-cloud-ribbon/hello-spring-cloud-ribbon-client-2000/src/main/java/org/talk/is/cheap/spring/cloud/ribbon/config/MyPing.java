package org.talk.is.cheap.spring.cloud.ribbon.config;

import com.netflix.loadbalancer.PingUrl;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dominiczhu
 * @version 1.0
 * @title MyPing
 * @date 2022/4/1 9:57 上午
 */
@Slf4j
public class MyPing extends PingUrl {
    @Override
    public boolean isAlive(Server server) {

        log.info("my ribbon ping, server: {}",server);

        return super.isAlive(server);
    }
}
