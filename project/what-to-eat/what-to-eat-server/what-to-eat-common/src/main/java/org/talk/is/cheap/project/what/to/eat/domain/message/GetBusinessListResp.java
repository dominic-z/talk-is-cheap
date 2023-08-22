package org.talk.is.cheap.project.what.to.eat.domain.message;

import lombok.Data;
import org.talk.is.cheap.project.what.to.eat.domain.bo.BusinessBO;

import java.util.List;

public class GetBusinessListResp extends GenericResponse<GetBusinessListResp.GetBusinessListRespBody> {
    @Data
    public static class GetBusinessListRespBody {
        private Integer page;

        private Integer pageSize;

        private Long total;
        private List<BusinessBO> businessBOs;
    }
}
