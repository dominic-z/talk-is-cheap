package org.talk.is.cheap.project.what.to.eat.domain.message;

import lombok.Data;

public class GetBusinessListReq extends GenericRequest<GetBusinessListReq.GetBusinessListReqBody>{

    @Data
    public static class GetBusinessListReqBody{
        private Integer page;

        private Integer pageSize;
    }
}
