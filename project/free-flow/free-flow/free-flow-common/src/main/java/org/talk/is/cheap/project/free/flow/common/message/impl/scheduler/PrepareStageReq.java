package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

public class PrepareStageReq extends HttpBody<PrepareStageReq.PrepareStageReqData> {

    @Data
    public static class PrepareStageReqData{
        private Long taskExecutionId;
        private String stageName;
        private String encodedSharedContextSnapshotAtStartup;

    }
}
