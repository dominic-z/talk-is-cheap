package org.talk.is.cheap.project.free.flow.common.message.impl.worker;

import lombok.Builder;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.List;


public class WorkerStartTaskReq extends HttpBody<WorkerStartTaskReq.Data> {

    @lombok.Data
    @Builder
    public static class Data {
        private Long taskExecutionId;
        private String taskName;
        private Integer taskVersion;
        private String encodedTaskStartupContext;
        private List<StartStageDatum> startStageData;
    }

    @lombok.Data
    @Builder
    public static class StartStageDatum {
        private Long stageExecutionId;
        private String stageName;
        private Integer stageVersion;
        private String encodedInput;

    }
}
