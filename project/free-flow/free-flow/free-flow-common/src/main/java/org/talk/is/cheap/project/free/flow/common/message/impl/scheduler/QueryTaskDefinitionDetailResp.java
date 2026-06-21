package org.talk.is.cheap.project.free.flow.common.message.impl.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.TaskDefinitionDTO;

import java.util.List;



public class QueryTaskDefinitionDetailResp extends HttpBody<QueryTaskDefinitionDetailResp.GetTaskDefinitionDetailRespData> {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetTaskDefinitionDetailRespData {

        private Integer page;
        private Integer pageSize;
        private Long total;

        private List<TaskDefinitionDTO> taskDefinitionDTOs;

    }
}
