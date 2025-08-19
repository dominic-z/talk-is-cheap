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



    public String assignTaskToWorkerId(String taskName, String[] candidateWorkerIds) {
        int i = Hashing.consistentHash(Hashing.sha256().hashString(taskName, StandardCharsets.UTF_8),
                candidateWorkerIds.length);
        return candidateWorkerIds[i];
    }

}
