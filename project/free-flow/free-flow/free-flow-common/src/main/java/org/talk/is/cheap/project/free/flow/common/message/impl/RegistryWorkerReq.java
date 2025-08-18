package org.talk.is.cheap.project.free.flow.common.message.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

public class RegistryWorkerReq extends HttpBody<RegistryWorkerReq.Data> {

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data{
        private String workerId;
        private String zkNodePath;
    }
}
