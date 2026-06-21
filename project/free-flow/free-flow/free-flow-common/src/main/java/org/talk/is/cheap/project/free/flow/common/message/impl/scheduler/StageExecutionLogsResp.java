package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class StageExecutionLogsResp extends HttpBody<List<StageExecutionLogsResp.StageExecutionLog>> {

    @lombok.Data
    public static class Data{

        private List<Object> searchAfter;
        private List<StageExecutionLogsResp.StageExecutionLog> logs;
    }

    @lombok.Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StageExecutionLog {
        private String logContent;
        private Date createTime;
        private List<Object> sort;
    }
}
