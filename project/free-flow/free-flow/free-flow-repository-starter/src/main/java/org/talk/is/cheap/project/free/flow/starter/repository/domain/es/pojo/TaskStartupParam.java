package org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo;

import lombok.Data;

@Data
public class TaskStartupParam {
    private Long taskStartupId;
    private String startupParamFullyQualifiedClassName;
    private String startupParamEncoding;
}
