package org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TaskSharedContext {
    private Long taskStartupId;
    private String encodedTaskSharedContext;
    private Date updateTime;

    public static String TASK_EXECUTION_ID = "task_execution_id";
}
