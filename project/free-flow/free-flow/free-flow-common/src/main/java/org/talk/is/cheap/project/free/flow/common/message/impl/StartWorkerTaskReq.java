package org.talk.is.cheap.project.free.flow.common.message.impl;

import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.List;
import java.util.Map;


public class StartWorkerTaskReq extends HttpBody<StartWorkerTaskReq.Data> {

    @lombok.Data
    public static class Data {
        private List<TaskStartupData> startupDataList;
    }

    @lombok.Data
    public static class TaskStartupData {
        private String taskName;
        private Integer taskVersion;
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
