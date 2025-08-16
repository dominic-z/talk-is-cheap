package org.talk.is.cheap.project.free.flow.common.message.impl;

import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.enums.TaskType;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;

import java.util.List;

public class QueryTaskDefinitionReq extends HttpBody<QueryTaskDefinitionReq.QueryTaskDefinitionReqData> {

    @Data
    public static class QueryTaskDefinitionReqData {
        @Data
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
