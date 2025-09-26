package org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class StageExecutionBizLog {
    private Long stageExecutionId;
    private String log;
    private Date createTime;
}
