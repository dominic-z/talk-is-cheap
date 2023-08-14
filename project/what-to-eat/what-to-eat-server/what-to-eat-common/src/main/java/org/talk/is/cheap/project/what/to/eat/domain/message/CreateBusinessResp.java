package org.talk.is.cheap.project.what.to.eat.domain.message;

import lombok.Data;

public class CreateBusinessResp extends GenericResponse<CreateBusinessResp.BusinessRespBody> {
    @Data
    public static class BusinessRespBody {
        private long id;
    }
}
