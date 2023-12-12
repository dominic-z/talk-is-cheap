package org.talk.is.cheap.project.what.to.eat.domain.message;

import lombok.Data;

import java.util.Date;

public class CreateMarketingPlanReq extends GenericRequest<CreateMarketingPlanReq.CreateMarketingPlanReqBody> {

    @Data
    public static class CreateMarketingPlanReqBody {

        private long businessId;

        private Date marketingStartTime;

        private Date marketingEndTime;

        private String cron;

        private String marketingTitle;

        private String marketingDetail;

    }
}
