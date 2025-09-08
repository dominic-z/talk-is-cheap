package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.StageExecutionMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.StageExecutionDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageExecutionExample;

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
* @date 2025/08/16
*/
@Service
public class StageExecutionService{

    @Autowired
    private StageExecutionDao stageExecutionDao;

    @Autowired
    private StageExecutionMapper stageExecutionMapper;

    // 基于StageExecutionMapper

    @Transactional(rollbackFor = Exception.class ,transactionManager = "repositoryStarterTransactionManager")
    public int create(StageExecution record) {
        if (record == null) {
            return 0;
        }
        return stageExecutionMapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int createBatch(Collection<StageExecution> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return stageExecutionMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int deleteByExample(StageExecutionExample example) {
        if (example == null) {
            return 0;
        }
        return stageExecutionMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int updateByExampleSelective(StageExecution record, StageExecutionExample example) {
        if (record == null || example == null) {
            return 0;
        }
        // record.setUpdateTime(new Date()); // 通过数据库触发器实现
        return stageExecutionMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(StageExecutionExample example) {
        if (example == null) {
            return 0L;
        }

        return stageExecutionMapper.countByExample(example);
    }

    public List<StageExecution> selectByExample(StageExecutionExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return stageExecutionMapper.selectByExample(example);
    }

    // 深度分页的service接口
    public List<StageExecution> selectByExampleDeepPaging(StageExecutionExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        if (example.getLimit() == null || example.getOffset() == null){
            throw new IllegalArgumentException("limit or offset can't be null");
        }
        return stageExecutionMapper.selectByExampleDeepPagingByIdSubQuery(example);
    }

    // 基于stageExecutionDao

}
