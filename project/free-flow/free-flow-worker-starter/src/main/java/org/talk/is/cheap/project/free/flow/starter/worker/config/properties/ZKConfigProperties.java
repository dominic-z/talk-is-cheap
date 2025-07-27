package org.talk.is.cheap.project.free.flow.starter.worker.config.properties;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Data
@Slf4j
public class ZKConfigProperties {

    @Data
    public static class Zookeeper {
        private String connectUrl;
        private Integer sessionTimeout;
        private Integer connectionTimeout;
        private String scheme;
        private String zkAuthId;

        private Path path;

        @Data
        public static class Path {
            private String worker;
            private String scheduler;
        }
    }

    @Data
    public static class RetryPolicy {
        private Integer baseSleepTime;
        private Integer maxRetries;
        private Integer maxSleep;
    }

    private Zookeeper zookeeper;
    private RetryPolicy retryPolicy;



}
