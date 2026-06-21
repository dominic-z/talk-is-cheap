package org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class StageExecutionBizLog {
    public static final String STAGE_EXECUTION_ID = "stage_execution_id";
    public static final String CREATE_TIME = "create_time";
    private Long stageExecutionId;
    private Long taskExecutionId;
    private String log;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
