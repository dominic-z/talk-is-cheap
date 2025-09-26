package org.talk.is.cheap.project.free.flow.common.message.impl;

import lombok.Data;
import lombok.Getter;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkerSubmitStageResultReq extends HttpBody<WorkerSubmitStageResultReq.Data> {

    @lombok.Data
    public static class Data {

        private List<StageResult> stageResultList;

    }

    @lombok.Data
    public static class StageResult {
        private Long stageStartupId;
        private Boolean succeeded;
        private String msg;
        private String encodedSharedContext;
        private Date completionTime;
    }

    public enum Result {
        SUCCEEDED(0, "succeeded"),
        FAILED(1, "failed"),
        ;
        @Getter
        final int result;
        @Getter
        final String desc;
        private final Map<Integer, Result> ENUM_MAP = new HashMap<>();

        Result(int result, String desc) {
            this.result = result;
            this.desc = desc;
            ENUM_MAP.put(result, this);
        }
    }
}
