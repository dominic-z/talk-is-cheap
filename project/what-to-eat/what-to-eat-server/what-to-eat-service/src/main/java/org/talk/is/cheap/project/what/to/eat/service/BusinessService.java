package org.talk.is.cheap.project.what.to.eat.service;

import lombok.val;
import org.talk.is.cheap.project.what.to.eat.dao.mbg.BusinessMapper;
import org.talk.is.cheap.project.what.to.eat.dao.customized.BusinessDao;
import org.talk.is.cheap.project.what.to.eat.domain.pojo.Business;
import org.talk.is.cheap.project.what.to.eat.domain.query.example.BusinessExample;

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
public class BusinessService {

    @Autowired
    private BusinessDao businessDao;

    @Autowired
    private BusinessMapper businessMapper;

    // 基于BusinessMapper

    @Transactional(rollbackFor = Exception.class)
    public int create(Business record) {
        if (record == null) {
            return 0;
        }
        return businessMapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public int createBatch(Collection<Business> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return businessMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByExample(BusinessExample example) {
        if (example == null) {
            return 0;
        }
        return businessMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByExampleSelective(Business record, BusinessExample example) {
        if (record == null || example == null) {
            return 0;
        }
        record.setUpdateTime(new Date());
        return businessMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(BusinessExample example) {
        if (example == null) {
            return 0L;
        }

        return businessMapper.countByExample(example);
    }

    public List<Business> selectByExample(BusinessExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return businessMapper.selectByExample(example);
    }

    // 基于businessDao


    // 基于自动生成的Mapper和Dao手动编写

    public Business selectByPrimaryKey(Long id) throws VerificationException {
        VerifyUtil.notNull(id, "business id is null");
        val businessExample = new BusinessExample();
        businessExample.createCriteria().andIdEqualTo(id).andStatusEqualTo(0);
        val businesses = selectByExample(businessExample);
        if (businesses.size() == 0) {
            return null;
        } else {
            return businesses.get(0);
        }
    }


    public int updateByPrimaryKey(Long id, Business business) throws VerificationException {
        VerifyUtil.notNull(id, "business id is null");
        VerifyUtil.notNull(business, "business is null");

        val businessExample = new BusinessExample();
        businessExample.createCriteria().andIdEqualTo(id).andStatusEqualTo(0);
        return updateByExampleSelective(business, businessExample);
    }
}
