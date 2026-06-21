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
public class StageStartupParam {
    private Long stageStartupId;
    private String encodedInput;
    private String encodedSharedContextSnapshotAtStartup;
    private String encodedSharedContextSnapshotAtCompletion;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    public static String STAGE_STARTUP_ID = "stage_startup_id";
}
