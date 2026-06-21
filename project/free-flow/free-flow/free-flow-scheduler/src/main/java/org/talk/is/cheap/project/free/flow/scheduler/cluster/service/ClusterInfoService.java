package org.talk.is.cheap.project.free.flow.scheduler.cluster.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.QueryClusterInfoResp;
import org.talk.is.cheap.project.free.flow.starter.repository.service.ClusterNodeService;

@Service
@Slf4j
public class ClusterInfoService {

    @Autowired
    private ClusterNodeService clusterNodeService;

    @Autowired
    private SchedulerClusterManager schedulerClusterManager;

    @Autowired
    private WorkerClusterManager workerClusterManager;

//    public QueryClusterInfoResp query()

}
