package org.talk.is.cheap.project.free.flow.starter.worker.cluster.event;

import org.springframework.context.ApplicationEvent;


/**
 * 代表worker已经注册进集群，并完成了与scheduler的交互，相当于clusterService的registryWorker完成了。
 */
public class WorkerRegisteredEvent extends ApplicationEvent {
    public WorkerRegisteredEvent(Object source) {
        super(source);
    }
}
