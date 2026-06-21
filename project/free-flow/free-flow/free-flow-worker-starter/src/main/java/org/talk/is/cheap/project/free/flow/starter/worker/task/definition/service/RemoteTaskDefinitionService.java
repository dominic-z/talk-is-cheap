package org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service;


import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.QueryTaskDefinitionDetailsReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.QueryTaskDefinitionDetailResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.TaskDefinitionDTO;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.client.SchedulerTaskDefinitionClient;
import org.talk.is.cheap.project.free.flow.starter.worker.cluster.service.WorkerNodeService;

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
    private WorkerNodeService workerNodeService;

    public List<TaskDefinitionDTO> getTaskDefinitionDTOs(List<Tuple2<String, Integer>> taskNameAndVersions) {
        URI schedulerLeaderUri = workerNodeService.getRandomSchedulerURI();

        List<QueryTaskDefinitionDetailsReq.QueryTaskDefinitionDetailsReqData.Query> queries =
                taskNameAndVersions.stream()
                        .map(t -> QueryTaskDefinitionDetailsReq.QueryTaskDefinitionDetailsReqData.Query.builder().taskName(t._1).taskVersion(t._2).build())
                        .toList();

        List<TaskDefinitionDTO> taskDefinitionDTOs = new ArrayList<>();
//        不断翻页。防止死循环
        for (int page = 1, pageSize = 20; page < 100; page++) {
            QueryTaskDefinitionDetailsReq.QueryTaskDefinitionDetailsReqData reqData =
                    QueryTaskDefinitionDetailsReq.QueryTaskDefinitionDetailsReqData.builder()
                            .queries(queries).page(page).pageSize(pageSize).build();
            QueryTaskDefinitionDetailsReq req = new QueryTaskDefinitionDetailsReq();
            req.setData(reqData);

            QueryTaskDefinitionDetailResp resp = schedulerTaskDefinitionClient.queryTaskDefinition(schedulerLeaderUri, req);
            VerifyUtil.requireTrue(resp.isSuccess(), String.format("can't query remote task definition, reason: %s", resp.getMsg()));
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
