package org.talk.is.cheap.project.free.flow.common.message.impl.worker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

public class WorkerRetryStageReq extends HttpBody<WorkerRetryStageReq.WorkerRetryStageReqData> {


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkerRetryStageReqData {
        private Long taskExecutionId;
        private String stageName;
        private Long stageExecutionId;
        private String encodedInput;
        // 如果重试任务stage，不允许重设encodedSharedContextSnapshotAtStartup
        // 因为sharedContext是整个任务共享的，可能其他阶段还在运行并且在执行操作sharedCOntenxt，如果只是某个stage重试导致重写了sharedContext，可能会导致其他任务错乱
//        private String encodedSharedContextSnapshotAtStartup;

    }
}
