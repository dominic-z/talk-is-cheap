package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.TaskSourceTargetStartupRelationMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.TaskSourceTargetStartupRelationDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskSourceTargetStartupRelation;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskSourceTargetStartupRelationExample;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Date;
import java.lang.IllegalArgumentException;

/**
* 定制化的service层，用于弥补mbg生成的mapper过于灵活导致可能出现的业务漏洞，例如越过deleted字段查询、更新updateTime等
* @author dominiczhu
* @date 2026/02/27
*/
@Service
public class TaskSourceTargetStartupRelationService{

    @Autowired
    private TaskSourceTargetStartupRelationDao taskSourceTargetStartupRelationDao;

    @Autowired
    private TaskSourceTargetStartupRelationMapper taskSourceTargetStartupRelationMapper;

    // 基于TaskSourceTargetStartupRelationMapper

    @Transactional(rollbackFor = Exception.class , transactionManager = "repositoryStarterTransactionManager")
    public int create(TaskSourceTargetStartupRelation record) {
        if (record == null) {
            return 0;
        }
        return taskSourceTargetStartupRelationMapper.insertSelective(record);
    }


    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int deleteByExample(TaskSourceTargetStartupRelationExample example) {
        if (example == null) {
            return 0;
        }
        return taskSourceTargetStartupRelationMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int updateByExampleSelective(TaskSourceTargetStartupRelation record, TaskSourceTargetStartupRelationExample example) {
        if (record == null || example == null) {
            return 0;
        }
        // record.setUpdateTime(new Date()); // 通过数据库触发器实现
        return taskSourceTargetStartupRelationMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(TaskSourceTargetStartupRelationExample example) {
        if (example == null) {
            return 0L;
        }

        return taskSourceTargetStartupRelationMapper.countByExample(example);
    }

    public List<TaskSourceTargetStartupRelation> selectByExample(TaskSourceTargetStartupRelationExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return taskSourceTargetStartupRelationMapper.selectByExample(example);
    }

    // 深度分页的service接口
    public List<TaskSourceTargetStartupRelation> selectByExampleDeepPaging(TaskSourceTargetStartupRelationExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        if (example.getLimit() == null || example.getOffset() == null){
            throw new IllegalArgumentException("limit or offset can't be null");
        }
        return taskSourceTargetStartupRelationMapper.selectByExampleDeepPagingByIdSubQuery(example);
    }

    // insertBatch的service接口
    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int createBatch(Collection<TaskSourceTargetStartupRelation> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return taskSourceTargetStartupRelationMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int createBatchSelective(Collection<TaskSourceTargetStartupRelation> records, Collection<String> excludeColNames) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return taskSourceTargetStartupRelationMapper.insertBatchSelective(records, new HashSet<String>(excludeColNames));
    }
    // 基于taskSourceTargetStartupRelationDao

}
