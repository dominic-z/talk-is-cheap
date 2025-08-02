package org.talk.is.cheap.project.free.flow.scheduler.task;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;

@Service
public class TaskManager {

    @Autowired
    private WorkerClusterManager workerClusterManager;

}
