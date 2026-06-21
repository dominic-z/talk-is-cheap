package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Date;
import java.util.List;

public class WorkerStartStageReportReq extends HttpBody<List<WorkerStartStageReportReq.WorkerStartToExecuteStageReqDatum>> {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkerStartToExecuteStageReqDatum {
        private Long taskExecutionId;
        private Long stageExecutionId;
        private Date startTime;

    }
}
