package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

public class WorkerFailTaskReq extends HttpBody<WorkerFailTaskReq.WorkerFailTaskReqData> {

    @Data
    public static class WorkerFailTaskReqData {
        private Long taskExecutionId;
        private Integer errorCode;
        private String errorMsg;
        private boolean pausing;
    }
}
