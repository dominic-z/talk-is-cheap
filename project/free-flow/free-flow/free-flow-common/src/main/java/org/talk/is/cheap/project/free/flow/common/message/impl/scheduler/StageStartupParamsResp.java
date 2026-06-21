package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.List;

public class StageStartupParamsResp extends HttpBody<List<StageStartupParamsResp.StageStartupParam>> {
    @Data
    public static class StageStartupParam {
        private Long stageStartupId;
        private String encodedSharedContextAtStartup;
        private String encodedSharedContextAtCompletion;
        private String encodedInput;
    }
}
