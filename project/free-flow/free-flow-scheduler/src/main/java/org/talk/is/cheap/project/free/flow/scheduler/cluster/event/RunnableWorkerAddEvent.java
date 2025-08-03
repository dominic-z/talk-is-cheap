package org.talk.is.cheap.project.free.flow.scheduler.cluster.event;

import org.springframework.context.ApplicationEvent;

public class RunnableWorkerAddEvent extends ApplicationEvent {
    public RunnableWorkerAddEvent(Object source) {
        super(source);
    }
}
