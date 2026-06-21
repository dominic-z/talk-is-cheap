package org.talk.is.cheap.project.free.flow.scheduler.cluster.controller.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.enums.NodeStatus;
import org.talk.is.cheap.project.free.flow.common.enums.NodeType;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.QueryClusterInfoReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.QueryClusterInfoResp;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.controller.ClusterManageController;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.SchedulerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.ClusterNodeExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNode;
import org.talk.is.cheap.project.free.flow.starter.repository.service.ClusterNodeService;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping
@Slf4j
public class ClusterManageControllerImpl implements ClusterManageController {

    @Autowired
    private SchedulerClusterManager schedulerClusterManager;
    @Autowired
    private WorkerClusterManager workerClusterManager;

    @Autowired
    private ClusterNodeService clusterNodeService;

    @Override
    public QueryClusterInfoResp queryNodes(QueryClusterInfoReq req) {
        QueryClusterInfoResp resp = new QueryClusterInfoResp();

        QueryClusterInfoReq.QueryClusterNodeReqData reqData = req.getData();

        if (reqData == null) {
            return resp;
        }
        
        if (NodeType.SCHEDULER.getType().equals(reqData.getNodeType()) || NodeType.WORKER.getType().equals(reqData.getNodeType())) {
            ClusterNodeExample example = new ClusterNodeExample();
            ClusterNodeExample.Criteria criteria =
                    example.createCriteria().andStatusEqualTo(NodeStatus.RUNNABLE.getStatus());
            ArrayList<Integer> nodeTypeCond = new ArrayList<>();
            nodeTypeCond.add(reqData.getNodeType());
            if(NodeType.SCHEDULER.getType().equals(reqData.getNodeType())){
                nodeTypeCond.add(NodeType.SCHEDULER_LEADER.getType());
            }
            criteria.andNodeTypeIn(nodeTypeCond);
            long total = clusterNodeService.countByExample(example);


            int page = reqData.getPage() == null || reqData.getPage() <= 0 ? 1 : reqData.getPage();
            int pageSize = reqData.getPageSize() == null || reqData.getPageSize() <= 0 || reqData.getPageSize() > 100 ?
                    10 : reqData.getPageSize();
            example.setOffset((page - 1) * pageSize);
            example.setLimit(pageSize);
            List<ClusterNode> clusterNodes = clusterNodeService.selectByExample(example);

            List<QueryClusterInfoResp.Node> respDataNodes = clusterNodes.stream().map(n ->
                    QueryClusterInfoResp.Node.builder().nodeAddress(n.getNodeAddress())
                            .nodeType(n.getNodeType())
                            .nodeStatus(schedulerClusterManager.isValid(n.getNodeZkPath()) ? NodeStatus.RUNNABLE.getStatus() :
                                    NodeStatus.TERMINATED.getStatus())
                            .launchTime(n.getUpdateTime())
                            .isLeader(schedulerClusterManager.isLeader(n.getNodeAddress()))
                            .build()).toList();

            QueryClusterInfoResp.QueryClusterNodeRespData respData = new QueryClusterInfoResp.QueryClusterNodeRespData();
            respData.setNodes(respDataNodes);
            respData.setPage(page);
            respData.setTotal(total);
            respData.setPageSize(pageSize);
            resp.success(respData);

        } else {
            QueryClusterInfoResp.QueryClusterNodeRespData respData = new QueryClusterInfoResp.QueryClusterNodeRespData();
            respData.setNodes(List.of());
            respData.setTotal(0);
            resp.success(respData);
        }

        return resp;
    }

    @Override
    public HttpBody<String> tryTerminateWorker(String workerAddress) {
        HttpBody<String> resp = new HttpBody<>();
        try{
            workerClusterManager.tryTerminate(workerAddress);
            resp.success();
        }catch (Exception e){
            resp.fail(ResultCode.FAIL,e.getMessage());
        }
        return resp;
    }
}
