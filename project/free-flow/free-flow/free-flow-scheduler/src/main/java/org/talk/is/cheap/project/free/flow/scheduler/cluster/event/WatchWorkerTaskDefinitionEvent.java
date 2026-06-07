package org.talk.is.cheap.project.free.flow.scheduler.cluster.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class WatchWorkerTaskDefinitionEvent extends ApplicationEvent {

    @Getter
    private final List<String> workerAddrs;

    @Getter
    private final boolean needInit; // 初始化，不监听任何特定节点

    public WatchWorkerTaskDefinitionEvent(boolean needInit, List<String> workerAddrs) {
        super(workerAddrs);
        this.needInit = needInit;
        this.workerAddrs = workerAddrs;
    }
}
