package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Map;

public class ReScheduleTaskReq extends HttpBody<ReScheduleTaskReq.Data> {

    @lombok.Data
    public static class Data {
        private Long taskExecutionId;
    }
}
