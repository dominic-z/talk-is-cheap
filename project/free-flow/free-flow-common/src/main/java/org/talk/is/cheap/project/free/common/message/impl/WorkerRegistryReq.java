package org.talk.is.cheap.project.free.common.message.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.common.message.HttpBody;

public class WorkerRegistryReq extends HttpBody<WorkerRegistryReq.Data> {

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data{
        private String workerId;
        private String zkNodePath;
    }
}
