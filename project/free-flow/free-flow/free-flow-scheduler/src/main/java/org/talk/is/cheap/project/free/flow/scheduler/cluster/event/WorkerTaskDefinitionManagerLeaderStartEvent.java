package org.talk.is.cheap.project.free.flow.scheduler.cluster.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class WorkerTaskDefinitionManagerLeaderStartEvent extends ApplicationEvent {

    @Getter
    private final boolean start;

    public WorkerTaskDefinitionManagerLeaderStartEvent(Boolean start) {
        super(start);
        this.start = start;
    }
}
