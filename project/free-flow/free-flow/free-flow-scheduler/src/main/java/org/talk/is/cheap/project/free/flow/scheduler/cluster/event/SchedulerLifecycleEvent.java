package org.talk.is.cheap.project.free.flow.scheduler.cluster.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class SchedulerLifecycleEvent extends ApplicationEvent {


    public enum SchedulerLifeCycle {
        ONLINE,
        OFFLINE
    }

    public enum Source {
        ZOOKEEPER, // 是监听zookeeper发现其他节点上线了
        MYSELF // 自己这个节点上线了
    }

    @Getter
    private SchedulerLifeCycle lifeCycle;

    @Getter
    private String address;


    public SchedulerLifecycleEvent(SchedulerLifeCycle lifeCycle, String address, Source source) {
        super(source);
        this.lifeCycle = lifeCycle;
        this.address = address;
    }

}
