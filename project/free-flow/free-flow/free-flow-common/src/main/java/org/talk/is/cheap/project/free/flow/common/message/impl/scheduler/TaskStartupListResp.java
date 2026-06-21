package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Date;
import java.util.List;

public class TaskStartupListResp extends HttpBody<TaskStartupListResp.TaskStartupListRespData> {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TaskStartupListRespData{
        private Integer page;
        private Integer pageSize;
        private Long total;
        private List<TaskStartupInfo> taskStartupInfoList;
    }

    @Data
    public static class TaskStartupInfo{
        private Long id;
        private String taskName;
        private Integer taskVersion;
        private Integer progress;
        private Integer status;
        private Date startupTime;
    }
}
