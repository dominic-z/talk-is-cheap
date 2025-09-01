package org.talk.is.cheap.project.free.flow.scheduler.cluster.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class RunnableWorkerAddEvent extends ApplicationEvent {

    @Getter
    private final String workerId;

    public RunnableWorkerAddEvent(String source) {
        super(source);
        this.workerId = source;
    }
}
