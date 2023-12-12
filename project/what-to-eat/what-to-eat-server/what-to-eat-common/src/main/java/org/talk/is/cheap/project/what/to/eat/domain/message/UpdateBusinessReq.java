package org.talk.is.cheap.project.what.to.eat.domain.message;

import lombok.Data;
import org.talk.is.cheap.project.what.to.eat.domain.bo.BusinessBO;

import java.util.List;

public class UpdateBusinessReq extends GenericRequest<UpdateBusinessReq.UpdateBusinessReqBody>{

    @Data
    public static class UpdateBusinessReqBody {
        private List<BusinessBO> businessBOList;
    }
}
