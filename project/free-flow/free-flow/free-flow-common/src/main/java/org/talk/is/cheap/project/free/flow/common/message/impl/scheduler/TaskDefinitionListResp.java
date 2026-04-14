package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.Date;
import java.util.List;


public class TaskDefinitionListResp extends HttpBody<TaskDefinitionListResp.Data> {

    @lombok.Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data{
        private long total;
        private List<Task> tasks;
    }

    @lombok.Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Task {
        private Long id;
        private String name;
        private Integer version;
        private Date releaseTime;
    }
}
