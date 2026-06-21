package org.talk.is.cheap.project.free.flow.scheduler.task.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;

import java.util.Random;
import java.util.Set;

@Service
public class TaskScheduler {

    @Autowired
    private WorkerClusterManager workerClusterManager;

    @Autowired
    private WorkerTaskDefinitionManager workerTaskDefinitionManager;


    public String assignTaskToWorkerAddress(String taskName, Integer taskVersion) {

        Set<String> workerAddressesWithTask = workerTaskDefinitionManager.getWorkerAddressesWithTaskRedis(taskName, taskVersion);

        VerifyUtil.requireFalse(workerAddressesWithTask.isEmpty(), "找不到可以运行%s,%d的worker".formatted(taskName, taskVersion));


        return (String) workerAddressesWithTask.toArray()[new Random().nextInt(workerAddressesWithTask.size())];


    }

}
