package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.StageStartupMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.StageStartupDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageStartupExample;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageCountGroupByTaskStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.lang.IllegalArgumentException;

/**
* 定制化的service层，用于弥补mbg生成的mapper过于灵活导致可能出现的业务漏洞，例如越过deleted字段查询、更新updateTime等
* @author dominiczhu
* @date 2025/12/16
*/
@Service
public class StageStartupService{

    @Autowired
    private StageStartupDao stageStartupDao;

    @Autowired
    private StageStartupMapper stageStartupMapper;

    // 基于StageStartupMapper

    @Transactional(rollbackFor = Exception.class , transactionManager = "repositoryStarterTransactionManager")
    public int create(StageStartup record) {
        if (record == null) {
            return 0;
        }
        return stageStartupMapper.insertSelective(record);
    }


    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int deleteByExample(StageStartupExample example) {
        if (example == null) {
            return 0;
        }
        return stageStartupMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int updateByExampleSelective(StageStartup record, StageStartupExample example) {
        if (record == null || example == null) {
            return 0;
        }
        // record.setUpdateTime(new Date()); // 通过数据库触发器实现
        return stageStartupMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(StageStartupExample example) {
        if (example == null) {
            return 0L;
        }

        return stageStartupMapper.countByExample(example);
    }

    public List<StageStartup> selectByExample(StageStartupExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return stageStartupMapper.selectByExample(example);
    }

    // 深度分页的service接口
    public List<StageStartup> selectByExampleDeepPaging(StageStartupExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        if (example.getLimit() == null || example.getOffset() == null){
            throw new IllegalArgumentException("limit or offset can't be null");
        }
        return stageStartupMapper.selectByExampleDeepPagingByIdSubQuery(example);
    }

    // insertBatch的service接口
    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int createBatch(Collection<StageStartup> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return stageStartupMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int createBatchSelective(Collection<StageStartup> records, Collection<String> excludeColNames) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return stageStartupMapper.insertBatchSelective(records, new HashSet<String>(excludeColNames));
    }
    // 基于stageStartupDao


    public List<StageCountGroupByTaskStatus> countGroupByTaskExecution(List<Long> taskExecutionIds){
        if(taskExecutionIds.isEmpty()){
            return new ArrayList<>();
        }
        return stageStartupDao.countGroupByTaskStatus(taskExecutionIds);
    }
}
