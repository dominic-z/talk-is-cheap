package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

public class RescheduleTaskReq extends HttpBody<RescheduleTaskReq.Data> {

    @lombok.Data
    public static class Data {
        private Long taskExecutionId;
    }
}
