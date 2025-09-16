package org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service;


import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.message.impl.QueryTaskDefinitionReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.QueryTaskDefinitionResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.TaskDefinitionDTO;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerTaskDefinitionClient;
import org.talk.is.cheap.project.free.flow.starter.worker.cluster.service.ClusterService;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 通过与scheduler交互对远端Task定义进行访问
 */
@Slf4j
@Service
public class RemoteTaskDefinitionService {


    @Autowired
    private SchedulerTaskDefinitionClient schedulerTaskDefinitionClient;

    @Autowired
    private ClusterService clusterService;

    public List<TaskDefinitionDTO> getTaskDefinitionDTOs(List<Tuple2<String, Integer>> taskNameAndVersions) {
        URI schedulerLeaderUri = clusterService.getRandomSchedulerURI();

        List<QueryTaskDefinitionReq.QueryTaskDefinitionReqData.Query> queries =
                taskNameAndVersions.stream()
                        .map(t -> QueryTaskDefinitionReq.QueryTaskDefinitionReqData.Query.builder().taskName(t._1).version(t._2).build())
                        .toList();

        List<TaskDefinitionDTO> taskDefinitionDTOs = new ArrayList<>();
//        不断翻页。防止死循环
        for (int page = 1, pageSize = 20; page < 100; page++) {
            QueryTaskDefinitionReq.QueryTaskDefinitionReqData reqData =
                    QueryTaskDefinitionReq.QueryTaskDefinitionReqData.builder().queries(queries).page(page).pageSize(pageSize).build();
            QueryTaskDefinitionReq req = new QueryTaskDefinitionReq();
            req.setData(reqData);

            QueryTaskDefinitionResp resp = schedulerTaskDefinitionClient.queryTaskDefinition(schedulerLeaderUri, req);
            VerifyUtil.shallBeTrue(resp.isSuccess(), String.format("can't query remote task definition, reason: %s", resp.getMsg()));
            if (req.getData() != null && !resp.getData().getTaskDefinitionDTOs().isEmpty()) {
                taskDefinitionDTOs.addAll(resp.getData().getTaskDefinitionDTOs());
            }

            if (resp.getData() == null || resp.getData().getTaskDefinitionDTOs().size() < page) {
                break;
            }
        }

        return taskDefinitionDTOs;
    }

}
