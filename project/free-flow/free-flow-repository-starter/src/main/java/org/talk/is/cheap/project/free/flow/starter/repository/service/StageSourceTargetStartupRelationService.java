package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.StageSourceTargetStartupRelationMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.StageSourceTargetStartupRelationDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageSourceTargetStartupRelation;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.query.example.StageSourceTargetStartupRelationExample;

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
* @date 2025/08/13
*/
@Service
public class StageSourceTargetStartupRelationService{

    @Autowired
    private StageSourceTargetStartupRelationDao stageSourceTargetStartupRelationDao;

    @Autowired
    private StageSourceTargetStartupRelationMapper stageSourceTargetStartupRelationMapper;

    // 基于StageSourceTargetStartupRelationMapper

    @Transactional(rollbackFor = Exception.class ,transactionManager = "repositoryStarterTransactionManager")
    public int create(StageSourceTargetStartupRelation record) {
        if (record == null) {
            return 0;
        }
        return stageSourceTargetStartupRelationMapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int createBatch(Collection<StageSourceTargetStartupRelation> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return stageSourceTargetStartupRelationMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int deleteByExample(StageSourceTargetStartupRelationExample example) {
        if (example == null) {
            return 0;
        }
        return stageSourceTargetStartupRelationMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int updateByExampleSelective(StageSourceTargetStartupRelation record, StageSourceTargetStartupRelationExample example) {
        if (record == null || example == null) {
            return 0;
        }
        record.setUpdateTime(new Date());
        return stageSourceTargetStartupRelationMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(StageSourceTargetStartupRelationExample example) {
        if (example == null) {
            return 0L;
        }

        return stageSourceTargetStartupRelationMapper.countByExample(example);
    }

    public List<StageSourceTargetStartupRelation> selectByExample(StageSourceTargetStartupRelationExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return stageSourceTargetStartupRelationMapper.selectByExample(example);
    }

    // 基于stageSourceTargetStartupRelationDao

}
