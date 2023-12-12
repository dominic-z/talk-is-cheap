package org.talk.is.cheap.project.what.to.eat.service;

import lombok.val;
import org.talk.is.cheap.project.what.to.eat.constants.Commons;
import org.talk.is.cheap.project.what.to.eat.constants.ErrorCode;
import org.talk.is.cheap.project.what.to.eat.dao.mbg.BusinessMarketingPlanMapper;
import org.talk.is.cheap.project.what.to.eat.dao.customized.BusinessMarketingPlanDao;
import org.talk.is.cheap.project.what.to.eat.domain.pojo.BusinessMarketingPlan;
import org.talk.is.cheap.project.what.to.eat.domain.query.example.BusinessMarketingPlanExample;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.talk.is.cheap.project.what.to.eat.exceptions.VerificationException;
import org.talk.is.cheap.project.what.to.eat.util.VerifyUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Date;

/**
 * 定制化的service层，用于弥补mbg生成的mapper过于灵活导致可能出现的业务漏洞，例如越过deleted字段查询、更新updateTime等
 *
 * @author dominiczhu
 * @date 2023/08/07
 */
@Service
public class BusinessMarketingPlanService {

    @Autowired
    private BusinessMarketingPlanDao businessMarketingPlanDao;

    @Autowired
    private BusinessMarketingPlanMapper businessMarketingPlanMapper;

    // 基于BusinessMarketingPlanMapper

    @Transactional(rollbackFor = Exception.class)
    public int create(BusinessMarketingPlan record) {
        if (record == null) {
            return 0;
        }
        return businessMarketingPlanMapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public int createBatch(Collection<BusinessMarketingPlan> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return businessMarketingPlanMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByExample(BusinessMarketingPlanExample example) {
        if (example == null) {
            return 0;
        }
        return businessMarketingPlanMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByExampleSelective(BusinessMarketingPlan record, BusinessMarketingPlanExample example) {
        if (record == null || example == null) {
            return 0;
        }
        record.setUpdateTime(new Date());
        return businessMarketingPlanMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(BusinessMarketingPlanExample example) {
        if (example == null) {
            return 0L;
        }

        return businessMarketingPlanMapper.countByExample(example);
    }

    public List<BusinessMarketingPlan> selectByExample(BusinessMarketingPlanExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return businessMarketingPlanMapper.selectByExample(example);
    }

    // 基于businessMarketingPlanDao

    // 基于自动生成的Mapper和Dao手动编写

    public List<BusinessMarketingPlan> selectByPrimaryKeys(List<Long> ids) throws VerificationException {
        VerifyUtil.isTrue(ids != null && !ids.isEmpty(), "id为空");
        VerifyUtil.gt(Commons.MAX_PAGE_SIZE, ids.size(), ErrorCode.LARGE_PAGE_SIZE_ERROR, "一次性查询数量过多");
        val example = new BusinessMarketingPlanExample();
        example.createCriteria().andIdIn(ids).andStatusEqualTo(0);
        example.setOrderByClause(BusinessMarketingPlan.ID);

        return selectByExample(example);
    }


    public List<BusinessMarketingPlan> selectByBusinessIds(List<Long> ids, int page, int pageSize) throws VerificationException {
        VerifyUtil.isTrue(ids != null && !ids.isEmpty(), "id为空");
        VerifyUtil.gt(page, 0, "page需要大于0");
        VerifyUtil.between(pageSize, 0, Commons.MAX_PAGE_SIZE, ErrorCode.LARGE_PAGE_SIZE_ERROR,
                "分页数字异常：%d".formatted(pageSize));

        val example = new BusinessMarketingPlanExample();
        example.createCriteria().andBusinessIdIn(ids).andStatusEqualTo(0);
        example.setOrderByClause(BusinessMarketingPlan.ID);
        example.setOffset(page * pageSize);
        example.setLimit(pageSize);

        return selectByExample(example);
    }


    @Transactional
    public int updateByPrimaryKey(Long id, BusinessMarketingPlan record) throws VerificationException {
        VerifyUtil.notNull(id, "id为null");
        VerifyUtil.notNull(record, "record is null");
        val example = new BusinessMarketingPlanExample();
        example.createCriteria().andIdEqualTo(id);
        return updateByExampleSelective(record, example);
    }


    @Transactional
    public int deleteByBusinessIds(List<Long> bIds) {
        val updateExample = new BusinessMarketingPlanExample();
        updateExample.createCriteria().andBusinessIdIn(bIds);
        return updateByExampleSelective(new BusinessMarketingPlan().withStatus(1), updateExample);

    }
}
