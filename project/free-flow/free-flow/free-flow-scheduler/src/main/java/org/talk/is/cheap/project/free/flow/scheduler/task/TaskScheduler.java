package org.talk.is.cheap.project.free.flow.scheduler.task;


import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.event.RunnableWorkerAddEvent;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

@Service
public class TaskScheduler {

    @Autowired
    private WorkerClusterManager workerClusterManager;

    // 耗时任务尽可能放在一个独立的线程里，避免影响主线程
    private final ThreadPoolExecutor taskDefinitionThreadPool = new ThreadPoolExecutor(0,1,1000, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

    @EventListener(RunnableWorkerAddEvent.class)
    public void onRunnableWorkerAddEvent(RunnableWorkerAddEvent event) {
        // todo: 多路复用改造
        // todo: 读取task中的任务定义

    }

    public String assignTaskToWorkerId(String taskName, String[] candidateWorkerIds) {
        int i = Hashing.consistentHash(Hashing.sha256().hashString(taskName, StandardCharsets.UTF_8),
                candidateWorkerIds.length);
        return candidateWorkerIds[i];
    }

}
