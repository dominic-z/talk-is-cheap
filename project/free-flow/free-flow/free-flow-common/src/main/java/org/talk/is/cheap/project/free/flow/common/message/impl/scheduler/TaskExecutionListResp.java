package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Date;
import java.util.List;

public class TaskExecutionListResp extends HttpBody<List<TaskExecutionListResp.TaskExecutionInfo>> {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TaskExecutionListRespData {
        private Integer page;
        private Integer pageSize;
        private Long total;
        private List<TaskExecutionInfo> taskExecutionInfoList;
    }

    @Data
    public static class TaskExecutionInfo {
        private Long id;
        private String assignedWorkerAddr;
        private Integer status;
        private Date completionTime;
        private Date startTime;
    }
}
