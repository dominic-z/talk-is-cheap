package org.talk.is.cheap.project.free.flow.scheduler.task.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.event.RunnableWorkerAddEvent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WorkerTaskDefinitionManager {

    // 耗时任务尽可能放在一个独立的线程里，避免影响主线程
    private final ThreadPoolExecutor taskDefinitionThreadPool = new ThreadPoolExecutor(0,1,1000, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());


    /**
     * 由Scheduler主动询问worker中的任务定义，因为目前scheduler就一个，因此由scheduler主动询问可以由scheduler控制访问的并发量。
     * @param event
     */
    @EventListener(RunnableWorkerAddEvent.class)
    public void onRunnableWorkerAddEvent(RunnableWorkerAddEvent event) {
        // todo: 多路复用改造
        // todo: 读取task中的任务定义

    }
}
