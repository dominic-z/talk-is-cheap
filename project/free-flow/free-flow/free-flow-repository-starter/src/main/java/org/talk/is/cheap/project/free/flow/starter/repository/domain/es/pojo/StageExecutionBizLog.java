package org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class StageExecutionBizLog {
    private Long stageExecutionId;
    private String log;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
