package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.enums.TaskType;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

public class QueryTaskDefinitionDetailsReq extends HttpBody<QueryTaskDefinitionDetailsReq.QueryTaskDefinitionDetailsReqData> {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QueryTaskDefinitionDetailsReqData {
        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Query{
            private TaskType taskType;
            private String taskName;
            private Integer taskVersion;
        }
        private Integer page;
        private Integer pageSize;
        private Query query;

    }
}
