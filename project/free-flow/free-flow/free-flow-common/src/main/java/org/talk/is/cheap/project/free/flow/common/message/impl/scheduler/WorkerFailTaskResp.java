package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

public class WorkerFailTaskResp extends HttpBody<WorkerFailTaskResp.WorkerFailTaskReqData> {

    @Data
    public static class WorkerFailTaskReqData {
        private Boolean retry;
    }
}
