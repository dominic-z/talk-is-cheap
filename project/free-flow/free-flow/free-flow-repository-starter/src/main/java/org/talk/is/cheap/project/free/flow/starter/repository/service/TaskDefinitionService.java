package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.TaskDefinitionMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.TaskDefinitionDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskDefinitionExample;

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
* @date 2026/04/01
*/
@Service
public class TaskDefinitionService{

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    // 基于TaskDefinitionMapper

    @Transactional(rollbackFor = Exception.class , transactionManager = "repositoryStarterTransactionManager")
    public int create(TaskDefinition record) {
        if (record == null) {
            return 0;
        }
        return taskDefinitionMapper.insertSelective(record);
    }


    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int deleteByExample(TaskDefinitionExample example) {
        if (example == null) {
            return 0;
        }
        return taskDefinitionMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int updateByExampleSelective(TaskDefinition record, TaskDefinitionExample example) {
        if (record == null || example == null) {
            return 0;
        }
        // record.setUpdateTime(new Date()); // 通过数据库触发器实现
        return taskDefinitionMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(TaskDefinitionExample example) {
        if (example == null) {
            return 0L;
        }

        return taskDefinitionMapper.countByExample(example);
    }

    public List<TaskDefinition> selectByExample(TaskDefinitionExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return taskDefinitionMapper.selectByExample(example);
    }

    // 深度分页的service接口
    public List<TaskDefinition> selectByExampleDeepPaging(TaskDefinitionExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        if (example.getLimit() == null || example.getOffset() == null){
            throw new IllegalArgumentException("limit or offset can't be null");
        }
        return taskDefinitionMapper.selectByExampleDeepPagingByIdSubQuery(example);
    }

    // insertBatch的service接口
    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int createBatch(Collection<TaskDefinition> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return taskDefinitionMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int createBatchSelective(Collection<TaskDefinition> records, Collection<String> excludeColNames) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return taskDefinitionMapper.insertBatchSelective(records, new HashSet<String>(excludeColNames));
    }
    // 基于taskDefinitionDao

}
