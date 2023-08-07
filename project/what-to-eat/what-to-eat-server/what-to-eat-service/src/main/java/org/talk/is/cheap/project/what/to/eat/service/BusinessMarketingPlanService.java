package org.talk.is.cheap.project.what.to.eat.service;

import org.talk.is.cheap.project.what.to.eat.dao.mbg.BusinessMarketingPlanMapper;
import org.talk.is.cheap.project.what.to.eat.dao.customized.BusinessMarketingPlanDao;
import org.talk.is.cheap.project.what.to.eat.domain.pojo.BusinessMarketingPlan;
import org.talk.is.cheap.project.what.to.eat.domain.query.example.BusinessMarketingPlanExample;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
* 定制化的service层，用于弥补mbg生成的mapper过于灵活导致可能出现的业务漏洞，例如越过deleted字段查询、更新updateTime等
* @author dominiczhu
* @date 2023/08/07
*/
@Service
public class BusinessMarketingPlanService{

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

}
