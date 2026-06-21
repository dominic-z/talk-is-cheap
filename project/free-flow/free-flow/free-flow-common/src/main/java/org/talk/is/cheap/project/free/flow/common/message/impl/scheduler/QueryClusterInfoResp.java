package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.Builder;
import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Date;
import java.util.List;

public class QueryClusterInfoResp extends HttpBody<QueryClusterInfoResp.QueryClusterNodeRespData> {


    @Data
    public static class QueryClusterNodeRespData{
        private int page;
        private int pageSize;
        private long total;
        private List<Node> nodes;
    }

    @Data
    @Builder
    public static class Node {
        private String nodeAddress;
        private Integer nodeType;
        private Boolean isLeader;
        private Integer nodeStatus;
        private Date launchTime;
    }


}
