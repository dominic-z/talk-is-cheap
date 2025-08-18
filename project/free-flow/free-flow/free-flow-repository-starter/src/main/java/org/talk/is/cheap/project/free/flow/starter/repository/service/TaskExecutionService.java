package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.TaskExecutionMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.TaskExecutionDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.query.example.TaskExecutionExample;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.lang.IllegalArgumentException;

/**
* 定制化的service层，用于弥补mbg生成的mapper过于灵活导致可能出现的业务漏洞，例如越过deleted字段查询、更新updateTime等
* @author dominiczhu
* @date 2025/08/16
*/
@Service
public class TaskExecutionService{

    @Autowired
    private TaskExecutionDao taskExecutionDao;

    @Autowired
    private TaskExecutionMapper taskExecutionMapper;

    // 基于TaskExecutionMapper

    @Transactional(rollbackFor = Exception.class ,transactionManager = "repositoryStarterTransactionManager")
    public int create(TaskExecution record) {
        if (record == null) {
            return 0;
        }
        return taskExecutionMapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int createBatch(Collection<TaskExecution> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return taskExecutionMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int deleteByExample(TaskExecutionExample example) {
        if (example == null) {
            return 0;
        }
        return taskExecutionMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int updateByExampleSelective(TaskExecution record, TaskExecutionExample example) {
        if (record == null || example == null) {
            return 0;
        }
        // record.setUpdateTime(new Date()); // 通过数据库触发器实现
        return taskExecutionMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(TaskExecutionExample example) {
        if (example == null) {
            return 0L;
        }

        return taskExecutionMapper.countByExample(example);
    }

    public List<TaskExecution> selectByExample(TaskExecutionExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return taskExecutionMapper.selectByExample(example);
    }

    // 深度分页的service接口
    public List<TaskExecution> selectByExampleDeepPaging(TaskExecutionExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        if (example.getLimit() == null || example.getOffset() == null){
            throw new IllegalArgumentException("limit or offset can't be null");
        }
        return taskExecutionMapper.selectByExampleDeepPagingByIdSubQuery(example);
    }

    // 基于taskExecutionDao

}
