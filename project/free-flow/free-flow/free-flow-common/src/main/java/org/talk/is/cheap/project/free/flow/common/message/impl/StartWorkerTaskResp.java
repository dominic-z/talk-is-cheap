package org.talk.is.cheap.project.free.flow.common.message.impl;

import com.fasterxml.jackson.annotation.JsonValue;
import io.vavr.Tuple2;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;

import java.util.List;
import java.util.Map;


public class StartWorkerTaskResp extends HttpBody<StartWorkerTaskResp.Data> {

    @lombok.Data
    public static class Data {
        private Map<Tuple2<String,Integer>,TaskStartResult> taskNameVersions;
    }

    @AllArgsConstructor
    public enum TaskStartResult{
        SUCCEEDED(0,""),
        FAILED(1,"FAILED"),
        NO_TASK_DEFINITION(2,"The task definition for this task is not available."),
        ;


        @Getter
        @JsonValue
        public final Integer code;
        @Getter
        public final String msg;
    }

}
