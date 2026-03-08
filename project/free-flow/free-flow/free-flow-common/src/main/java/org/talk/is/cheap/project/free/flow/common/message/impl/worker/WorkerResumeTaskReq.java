package org.talk.is.cheap.project.free.flow.common.message.impl.worker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Map;
import java.util.Set;


public class WorkerResumeTaskReq extends HttpBody<WorkerResumeTaskReq.Data> {

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
        private Map<String, Integer> stageFailedCount;
        private Set<String> succeedStageNames;
        private Integer taskFailedCount;
    }

}
