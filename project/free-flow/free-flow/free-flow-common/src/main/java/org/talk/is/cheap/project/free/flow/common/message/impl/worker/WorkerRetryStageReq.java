package org.talk.is.cheap.project.free.flow.common.message.impl.worker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

public class WorkerRetryStageReq extends HttpBody<WorkerRetryStageReq.WorkerRetryStageReqData> {


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkerRetryStageReqData {
        private Long taskExecutionId;
        private String stageName;
        private Long stageExecutionId;
        private String encodedInput;

    }
}
