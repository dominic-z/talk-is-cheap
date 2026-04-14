package org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo;


import lombok.Data;

@Data
public class StageCountGroupByTaskStatus {
    private Long taskExecutionId;
    private Long completedCount;
    private Long unfinishedCount;
}
