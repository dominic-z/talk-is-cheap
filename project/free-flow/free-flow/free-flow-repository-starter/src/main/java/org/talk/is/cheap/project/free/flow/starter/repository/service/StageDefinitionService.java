package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.StageDefinitionMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.StageDefinitionDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageDefinitionExample;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.lang.IllegalArgumentException;

/**
* 定制化的service层，用于弥补mbg生成的mapper过于灵活导致可能出现的业务漏洞，例如越过deleted字段查询、更新updateTime等
* @author dominiczhu
* @date 2025/09/04
*/
@Service
public class StageDefinitionService{

    @Autowired
    private StageDefinitionDao stageDefinitionDao;

    @Autowired
    private StageDefinitionMapper stageDefinitionMapper;

    // 基于StageDefinitionMapper

    @Transactional(rollbackFor = Exception.class ,transactionManager = "repositoryStarterTransactionManager")
    public int create(StageDefinition record) {
        if (record == null) {
            return 0;
        }
        return stageDefinitionMapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int createBatch(Collection<StageDefinition> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return stageDefinitionMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int deleteByExample(StageDefinitionExample example) {
        if (example == null) {
            return 0;
        }
        return stageDefinitionMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int updateByExampleSelective(StageDefinition record, StageDefinitionExample example) {
        if (record == null || example == null) {
            return 0;
        }
        // record.setUpdateTime(new Date()); // 通过数据库触发器实现
        return stageDefinitionMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(StageDefinitionExample example) {
        if (example == null) {
            return 0L;
        }

        return stageDefinitionMapper.countByExample(example);
    }

    public List<StageDefinition> selectByExample(StageDefinitionExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return stageDefinitionMapper.selectByExample(example);
    }

    // 深度分页的service接口
    public List<StageDefinition> selectByExampleDeepPaging(StageDefinitionExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        if (example.getLimit() == null || example.getOffset() == null){
            throw new IllegalArgumentException("limit or offset can't be null");
        }
        return stageDefinitionMapper.selectByExampleDeepPagingByIdSubQuery(example);
    }

    // 基于stageDefinitionDao

}
