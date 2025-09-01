package org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo;

import lombok.Data;

@Data
public class StageStartupParam {
    private Long stageStartupId;
    private String startupParamEncoding;
    private String sharedContextEncodingSnapshot;
}
