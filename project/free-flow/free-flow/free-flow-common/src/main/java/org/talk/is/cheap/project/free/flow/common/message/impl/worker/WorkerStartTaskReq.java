package org.talk.is.cheap.project.free.flow.common.message.impl.worker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Map;


public class WorkerStartTaskReq extends HttpBody<WorkerStartTaskReq.Data> {

    @lombok.Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {
        private Long taskExecutionId;
        private String taskName;
        private Integer taskVersion;
        private String initialEncodedSharedContext;
        // 包含所有stage的input
        private Map<String, String> stageEncodedInputs;
        // 仅仅包含启动的stage的信息
        private Map<String, Long> startingStageExecutionId;
        // 这个task已经失败的次数
        private Integer taskFailedCount;
    }

    @lombok.Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StartStageDatum {
        private Long stageExecutionId;
        private String stageName;

    }
}
