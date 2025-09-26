package org.talk.is.cheap.project.free.flow.scheduler.task;


import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.task.service.WorkerTaskDefinitionManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;

@Service
public class TaskScheduler {

    @Autowired
    private WorkerClusterManager workerClusterManager;

    @Autowired
    private WorkerTaskDefinitionManager workerTaskDefinitionManager;


    public String assignTaskToWorkerAddress(String taskName, Integer taskVersion) {

        Set<String> runnableWorkerNodeAddresses = workerClusterManager.getRunnableWorkerNodeAddresses();
        Set<String> workerAddressesWithTask = workerTaskDefinitionManager.getWorkerAddressesWithTask(taskName, taskVersion);


        runnableWorkerNodeAddresses.retainAll(workerAddressesWithTask);
        int i = Hashing.consistentHash(Hashing.sha256().hashString(taskName, StandardCharsets.UTF_8),
                runnableWorkerNodeAddresses.size());

        return new ArrayList<String>(runnableWorkerNodeAddresses).get(i);
    }

}
