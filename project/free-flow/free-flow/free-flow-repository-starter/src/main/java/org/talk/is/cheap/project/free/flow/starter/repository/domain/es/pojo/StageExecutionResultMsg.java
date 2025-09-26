package org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class StageExecutionResultMsg {
    private Long stageExecutionId;
    private String msg;
    private Date createTime;
}
