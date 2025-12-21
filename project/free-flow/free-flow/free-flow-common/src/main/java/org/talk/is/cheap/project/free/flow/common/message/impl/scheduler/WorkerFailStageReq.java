package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

public class WorkerFailStageReq extends HttpBody<WorkerFailStageReq.WorkerFailStageReqData> {

    @Data
    public static class WorkerFailStageReqData{
        private Long taskExecutionId;
        private Long stageExecutionId;

    }
}
