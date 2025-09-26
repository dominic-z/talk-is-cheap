package org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class StageStartupParam {
    private Long stageStartupId;
    private String encodedInput;
    private String encodedSharedContextSnapshotAtStartup;
    private String encodedSharedContextSnapshotAtCompletion;
    private Date updateTime;
}
