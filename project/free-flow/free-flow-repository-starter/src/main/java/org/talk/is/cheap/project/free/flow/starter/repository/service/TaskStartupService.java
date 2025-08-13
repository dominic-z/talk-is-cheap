package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.TaskStartupMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.TaskStartupDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.query.example.TaskStartupExample;

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
public class TaskStartupService{

    @Autowired
    private TaskStartupDao taskStartupDao;

    @Autowired
    private TaskStartupMapper taskStartupMapper;

    // 基于TaskStartupMapper

    @Transactional(rollbackFor = Exception.class ,transactionManager = "repositoryStarterTransactionManager")
    public int create(TaskStartup record) {
        if (record == null) {
            return 0;
        }
        return taskStartupMapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int createBatch(Collection<TaskStartup> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return taskStartupMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int deleteByExample(TaskStartupExample example) {
        if (example == null) {
            return 0;
        }
        return taskStartupMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int updateByExampleSelective(TaskStartup record, TaskStartupExample example) {
        if (record == null || example == null) {
            return 0;
        }
        record.setUpdateTime(new Date());
        return taskStartupMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(TaskStartupExample example) {
        if (example == null) {
            return 0L;
        }

        return taskStartupMapper.countByExample(example);
    }

    public List<TaskStartup> selectByExample(TaskStartupExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return taskStartupMapper.selectByExample(example);
    }

    // 基于taskStartupDao

}
