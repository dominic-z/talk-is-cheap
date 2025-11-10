package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.enums.TaskType;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.List;

public class QueryTaskDefinitionReq extends HttpBody<QueryTaskDefinitionReq.QueryTaskDefinitionReqData> {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QueryTaskDefinitionReqData {
        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Query{
            private TaskType taskType;
            private String taskName;
            private Integer version;
        }
        private Integer page;
        private Integer pageSize;
        private List<Query> queries;

    }
}
