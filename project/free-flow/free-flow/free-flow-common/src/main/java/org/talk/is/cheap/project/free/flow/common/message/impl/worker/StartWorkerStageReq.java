package org.talk.is.cheap.project.free.flow.common.message.impl.worker;

import lombok.Builder;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.List;


public class StartWorkerStageReq extends HttpBody<StartWorkerStageReq.Data> {

    @lombok.Data
    @Builder
    public static class Data {
        private List<StageStartupData> startupDataList;
    }

    @lombok.Data
    public static class StageStartupData{
        private String taskName;
        private Integer taskVersion;
        private Long taskStartupId;
        private String encodedSharedContext;
        private String stageName;
        private Integer stageVersion;
        private Long stageStartupId;
        private String encodedInput;

    }
}
