package org.talk.is.cheap.project.free.flow.scheduler.config.property;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "apache.zookeeper.path")
@Data
public class ZKPathProperty {

    @Data
    public static class Scheduler{
        private String root;
        private String election;
    }

    @Data
    public static class Worker{

        private String online;
        private String runnable;
        private String terminating;
    }

    private Scheduler scheduler;
    private Worker worker;

}
