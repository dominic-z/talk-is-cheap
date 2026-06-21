package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

public class PrepareStageResp extends HttpBody<PrepareStageResp.PrepareStageRespData> {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrepareStageRespData {
        private Long stageExecutionId;
        private String encodedInput;
    }
}
