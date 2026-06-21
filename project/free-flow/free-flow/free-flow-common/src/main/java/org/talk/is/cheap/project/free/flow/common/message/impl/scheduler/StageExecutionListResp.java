package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Date;
import java.util.List;

public class StageExecutionListResp extends HttpBody<List<StageExecutionListResp.StageExecutionDTO>> {
    @Data
    public static class StageExecutionDTO {
        private Long id;
        private Long stageStartupId;
        private Integer status;
        private Date startTime;

    }
}
