package org.talk.is.cheap.project.free.flow.scheduler.cluster.event;

import org.springframework.context.ApplicationEvent;

public class RunnableWorkerModifyEvent extends ApplicationEvent {
    public RunnableWorkerModifyEvent(Object source) {
        super(source);
    }
}
