package org.talk.is.cheap.project.what.to.eat.domain.message;

import lombok.Data;

public class DeleteBusinessReq extends GenericRequest<DeleteBusinessReq.DeleteBusinessReqBody> {

    @Data
    public static class DeleteBusinessReqBody {
        private Long businessId;
    }
}
