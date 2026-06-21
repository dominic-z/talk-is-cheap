package org.talk.is.cheap.project.free.flow.common.message.impl.worker;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;


public class WorkerResumeTaskResp extends HttpBody<WorkerResumeTaskResp.Data> {

    @lombok.Data
    public static class Data {
//        private List<StageStartResult> stageStartResultList;
    }

    @lombok.Data
    public static class StageStartResult {
        private Long stageStartupId;
        private Result result;

        private String msg;

        @AllArgsConstructor
        public enum Result {
            SUCCEEDED(0,""),
            FAILED(1,"FAILED"),
            NO_TASK_DEFINITION(2,"The task definition for this task is not available."),
            ;


            @Getter
            @JsonValue
            public final Integer code;
            @Getter
            public final String msg;
        }

    }

}
