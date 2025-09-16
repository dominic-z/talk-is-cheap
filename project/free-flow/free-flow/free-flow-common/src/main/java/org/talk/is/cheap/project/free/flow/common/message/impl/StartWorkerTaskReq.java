package org.talk.is.cheap.project.free.flow.common.message.impl;

import io.vavr.Tuple2;
import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Map;


public class StartWorkerTaskReq extends HttpBody<StartWorkerTaskReq.Data> {

    @lombok.Data
    public static class Data {
        private Map<Tuple2<String,Integer>, TaskStartupData> taskNameVersionParams;
    }

    @lombok.Data
    public static class TaskStartupData {
        private Long taskStartupId;
        private String encodedSharedContext;
        private Map<String,StageStartupData> stageStartupDataMap;
    }

    @lombok.Data
    public static class StageStartupData{
        private Long stageStartupId;
        private String encodedInput;
    }
}
