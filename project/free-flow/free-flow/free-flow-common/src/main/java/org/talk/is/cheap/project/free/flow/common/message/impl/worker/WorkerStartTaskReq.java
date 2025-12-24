package org.talk.is.cheap.project.free.flow.common.message.impl.worker;

import lombok.Builder;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Map;


public class WorkerStartTaskReq extends HttpBody<WorkerStartTaskReq.Data> {

    @lombok.Data
    @Builder
    public static class Data {
        private Long taskExecutionId;
        private String taskName;
        private Integer taskVersion;
        private String initialEncodedSharedContext;
        // 包含所有stage的input
        private Map<String,String> stageEncodedInputs;
        // 仅仅包含启动的stage的信息
        private Map<String,Long> startingStageExecutionId;
    }

    @lombok.Data
    @Builder
    public static class StartStageDatum {
        private Long stageExecutionId;
        private String stageName;

    }
}
