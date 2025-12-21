package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

public class WorkerFailStageResp extends HttpBody<WorkerFailStageResp.WorkerFailStageReqData> {

    @Data
    public static class WorkerFailStageReqData{
        private Boolean retry;
    }
}
