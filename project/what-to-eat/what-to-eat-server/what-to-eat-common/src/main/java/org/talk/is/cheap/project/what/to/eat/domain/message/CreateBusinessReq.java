package org.talk.is.cheap.project.what.to.eat.domain.message;

import lombok.Data;

public class CreateBusinessReq extends GenericRequest<CreateBusinessReq.BusinessReqBody>{

    @Data
    public static class BusinessReqBody{
        private String name;
        private String description;

    }
}
