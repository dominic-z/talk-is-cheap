package org.talk.is.cheap.project.what.to.eat.domain.message;

import lombok.Data;

import java.util.List;

public class DeleteMarketingPlanReq extends GenericRequest<DeleteMarketingPlanReq.DeleteMarketingPlanReqBody> {

    @Data
    public static class DeleteMarketingPlanReqBody {
        private List<Long> businessIds;
        private List<Long> marketingPlanIds;
    }
}
