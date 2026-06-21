package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Date;
import java.util.List;

public class StageStartupListResp extends HttpBody<List<StageStartupListResp.StageStartupInfo>> {
    @Data
    public static class StageStartupInfo{
        private Long id;
        private String taskExecutionId;
        private Integer stageId;
        private Integer status;
        private Integer failCount;
        private Date startTime;
        private Date completionTime;
    }
}
