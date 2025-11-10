package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Map;

public class StartTaskReq extends HttpBody<StartTaskReq.Data> {

    @lombok.Data
    public static class  Data{
        private String taskName;
        private Integer taskVersion;
        private String initialEncodedSharedContext;
        private Map<String,String> stageEncodedInputs;
    }
}
