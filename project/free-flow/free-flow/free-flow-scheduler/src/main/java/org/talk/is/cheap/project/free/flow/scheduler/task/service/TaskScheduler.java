package org.talk.is.cheap.project.free.flow.scheduler.task.service;


import com.google.common.hash.Hashing;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.task.service.WorkerTaskDefinitionManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;

@Service
public class TaskScheduler {

    @Autowired
    private WorkerClusterManager workerClusterManager;

    @Autowired
    private WorkerTaskDefinitionManager workerTaskDefinitionManager;


    public String assignTaskToWorkerAddress(String taskName, Integer taskVersion) {

        Set<String> workerAddressesWithTask = workerTaskDefinitionManager.getWorkerAddressesWithTask(taskName, taskVersion);


//        ArrayList<String> addresses = new ArrayList<>(workerAddressesWithTask);
//        addresses.sort(StringUtils::compare);

        // 说实话，目前用一致性hash没啥实际意义，只是看的花哨
//        int i = Hashing.consistentHash(Hashing.sha256().hashLong(taskStartupId),
//                addresses.size());

//        return addresses.get(i);

        return (String) workerAddressesWithTask.toArray()[new Random().nextInt(workerAddressesWithTask.size())];


    }

}
