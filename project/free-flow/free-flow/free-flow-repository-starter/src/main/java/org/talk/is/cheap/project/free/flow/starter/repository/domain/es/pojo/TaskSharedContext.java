package org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TaskSharedContext {

    public static final String TASK_STARTUP_ID = "task_startup_id";
    private Long taskStartupId;
    private String encodedTaskSharedContext;
    private Date updateTime;
    public static String TASK_EXECUTION_ID = "task_execution_id";
}
