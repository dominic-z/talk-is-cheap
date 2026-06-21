package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.TaskGraphDefinitionMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.TaskGraphDefinitionDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskGraphDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskGraphDefinitionExample;

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
* @date 2025/09/22
*/
@Service
public class TaskGraphDefinitionService{

    @Autowired
    private TaskGraphDefinitionDao taskGraphDefinitionDao;

    @Autowired
    private TaskGraphDefinitionMapper taskGraphDefinitionMapper;

    // 基于TaskGraphDefinitionMapper

    @Transactional(rollbackFor = Exception.class , transactionManager = "repositoryStarterTransactionManager")
    public int create(TaskGraphDefinition record) {
        if (record == null) {
            return 0;
        }
        return taskGraphDefinitionMapper.insertSelective(record);
    }


    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int deleteByExample(TaskGraphDefinitionExample example) {
        if (example == null) {
            return 0;
        }
        return taskGraphDefinitionMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int updateByExampleSelective(TaskGraphDefinition record, TaskGraphDefinitionExample example) {
        if (record == null || example == null) {
            return 0;
        }
        // record.setUpdateTime(new Date()); // 通过数据库触发器实现
        return taskGraphDefinitionMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(TaskGraphDefinitionExample example) {
        if (example == null) {
            return 0L;
        }

        return taskGraphDefinitionMapper.countByExample(example);
    }

    public List<TaskGraphDefinition> selectByExample(TaskGraphDefinitionExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return taskGraphDefinitionMapper.selectByExample(example);
    }

    // 深度分页的service接口
    public List<TaskGraphDefinition> selectByExampleDeepPaging(TaskGraphDefinitionExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        if (example.getLimit() == null || example.getOffset() == null){
            throw new IllegalArgumentException("limit or offset can't be null");
        }
        return taskGraphDefinitionMapper.selectByExampleDeepPagingByIdSubQuery(example);
    }

    // insertBatch的service接口
    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int createBatch(Collection<TaskGraphDefinition> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return taskGraphDefinitionMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int createBatchSelective(Collection<TaskGraphDefinition> records, Collection<String> excludeColNames) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return taskGraphDefinitionMapper.insertBatchSelective(records, new HashSet<String>(excludeColNames));
    }
    // 基于taskGraphDefinitionDao

}
