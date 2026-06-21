package org.talk.is.cheap.project.free.flow.scheduler.cluster.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class WorkerTerminatedEvent extends ApplicationEvent {

    @Getter
    private final String nodeAddress;

    public WorkerTerminatedEvent(String source) {
        super(source);
        this.nodeAddress = source;
    }
}
