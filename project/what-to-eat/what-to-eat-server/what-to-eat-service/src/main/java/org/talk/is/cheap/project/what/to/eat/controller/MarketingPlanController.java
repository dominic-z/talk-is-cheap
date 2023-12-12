package org.talk.is.cheap.project.what.to.eat.controller;


import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.web.bind.annotation.*;
import org.talk.is.cheap.project.what.to.eat.constants.Commons;
import org.talk.is.cheap.project.what.to.eat.constants.ErrorCode;
import org.talk.is.cheap.project.what.to.eat.domain.message.*;
import org.talk.is.cheap.project.what.to.eat.domain.pojo.BusinessMarketingPlan;
import org.talk.is.cheap.project.what.to.eat.exceptions.VerificationException;
import org.talk.is.cheap.project.what.to.eat.service.BusinessMarketingPlanService;
import org.talk.is.cheap.project.what.to.eat.service.BusinessService;
import org.talk.is.cheap.project.what.to.eat.util.JudgmentUtil;
import org.talk.is.cheap.project.what.to.eat.util.ResultUtil;
import org.talk.is.cheap.project.what.to.eat.util.VerifyUtil;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/api")
@CrossOrigin("http://localhost:3003/")
public class MarketingPlanController {

    @Autowired
    private BusinessService businessService;

    @Autowired
    private BusinessMarketingPlanService businessMarketingPlanService;

    @ResponseBody
    @RequestMapping(value = "/marketingPlan", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateMarketingPlanResp createMarketingPlan(@RequestBody CreateMarketingPlanReq req) {
        val resp = new CreateMarketingPlanResp();

        try {
            val data = req.getData();
            VerifyUtil.notNull(data, "请求体为空");
            VerifyUtil.notNull(data.getBusinessId(), ErrorCode.ILLEGAL_PARAMETER_ERROR, "businessId不可为空");
            VerifyUtil.notNull(data.getMarketingTitle(), "活动名称为空");
            VerifyUtil.isTrue(CronExpression.isValidExpression(data.getCron()),
                    "Cron表达式不合法： %s".formatted(data.getCron()));
            if (data.getMarketingStartTime() != null && data.getMarketingEndTime() != null) {
                VerifyUtil.gt(data.getMarketingEndTime(), data.getMarketingStartTime(), "活动的结束时间要早于起始时间");
            }

            VerifyUtil.notEmpty(businessService.selectByPrimaryKeys(List.of(data.getBusinessId())),
                    ErrorCode.DATA_NOT_FOUND_ERROR, "未找到企业");

            val businessMarketingPlan = new BusinessMarketingPlan()
                    .withBusinessId(data.getBusinessId())
                    .withCron(data.getCron())
                    .withMarketingTitle(data.getMarketingTitle())
                    .withMarketingDetail(data.getMarketingDetail())
                    .withMarketingStartTime(data.getMarketingStartTime())
                    .withMarketingEndTime(data.getMarketingEndTime())
                    .withStatus(0);
            businessMarketingPlanService.create(businessMarketingPlan);

            return ResultUtil.success(resp);
        } catch (VerificationException e) {
            log.error("校验异常", e);
            return ResultUtil.fail(resp, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("other 异常", e);
            return ResultUtil.fail(resp, ErrorCode.ERROR, e.getMessage());
        }

    }

    @RequestMapping(value = "/marketingPlan", method = RequestMethod.DELETE, consumes =
            MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public DeleteMarketingPlanResp deleteMarketingPlan(@RequestBody DeleteMarketingPlanReq req) {
        val resp = new DeleteMarketingPlanResp();
        try {
            val data = req.getData();
            VerifyUtil.notNull(data, "请求数据为空");
            val planIdsNullOrEmpty = JudgmentUtil.isNullOrIsEmpty(data.getMarketingPlanIds());
            val businessIdsNullOrEmpty = JudgmentUtil.isNullOrIsEmpty(data.getBusinessIds());
            VerifyUtil.isTrue((planIdsNullOrEmpty && !businessIdsNullOrEmpty) || (!planIdsNullOrEmpty && businessIdsNullOrEmpty),
                    "商户ID与营销活动ID不可同时指定，或者未指定任何商户或者营销活动");
            if (planIdsNullOrEmpty) {
                VerifyUtil.gt(Commons.MAX_PAGE_SIZE, data.getMarketingPlanIds().size(), ErrorCode.LARGE_PAGE_SIZE_ERROR,
                        "一次性删除过多的营销计划");
                val plans = businessMarketingPlanService.selectByPrimaryKeys(data.getMarketingPlanIds());

                for (BusinessMarketingPlan p : plans) {
                    businessMarketingPlanService
                            .updateByPrimaryKey(p.getId(), p.withStatus(1).withRevision(p.getRevision() + 1));
                }

            } else {
                VerifyUtil.gt(10,data.getBusinessIds().size(),ErrorCode.LARGE_PAGE_SIZE_ERROR,"一次最多删除10个商户");

                businessMarketingPlanService.deleteByBusinessIds(data.getBusinessIds());
            }

            return ResultUtil.success(resp);
        } catch (Exception e) {
            log.error("其他异常", e);
            return ResultUtil.fail(resp, ErrorCode.ERROR, e.getMessage());
        }
    }


}
