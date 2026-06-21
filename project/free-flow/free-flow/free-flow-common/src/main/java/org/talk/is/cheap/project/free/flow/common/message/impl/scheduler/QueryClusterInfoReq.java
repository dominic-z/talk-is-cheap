package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.List;

public class QueryClusterInfoReq extends HttpBody<QueryClusterInfoReq.QueryClusterNodeReqData> {

    @Data
    public static class QueryClusterNodeReqData {
        private Integer nodeType;
        private Integer pageSize;
        private Integer page;
    }
}
