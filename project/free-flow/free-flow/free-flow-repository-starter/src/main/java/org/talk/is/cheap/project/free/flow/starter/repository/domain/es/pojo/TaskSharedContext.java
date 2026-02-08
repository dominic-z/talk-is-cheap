package org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskSharedContext {

    public static final String TASK_STARTUP_ID = "task_startup_id";
    private Long taskStartupId;
    private String encodedTaskSharedContext;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    public static String TASK_EXECUTION_ID = "task_execution_id";
}
