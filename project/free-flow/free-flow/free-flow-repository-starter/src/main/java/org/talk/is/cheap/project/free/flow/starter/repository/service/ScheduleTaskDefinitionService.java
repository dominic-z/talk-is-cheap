package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.ScheduleTaskDefinitionMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.ScheduleTaskDefinitionDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ScheduleTaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.ScheduleTaskDefinitionExample;

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
public class ScheduleTaskDefinitionService{

    @Autowired
    private ScheduleTaskDefinitionDao scheduleTaskDefinitionDao;

    @Autowired
    private ScheduleTaskDefinitionMapper scheduleTaskDefinitionMapper;

    // 基于ScheduleTaskDefinitionMapper

    @Transactional(rollbackFor = Exception.class , transactionManager = "repositoryStarterTransactionManager")
    public int create(ScheduleTaskDefinition record) {
        if (record == null) {
            return 0;
        }
        return scheduleTaskDefinitionMapper.insertSelective(record);
    }


    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int deleteByExample(ScheduleTaskDefinitionExample example) {
        if (example == null) {
            return 0;
        }
        return scheduleTaskDefinitionMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int updateByExampleSelective(ScheduleTaskDefinition record, ScheduleTaskDefinitionExample example) {
        if (record == null || example == null) {
            return 0;
        }
        // record.setUpdateTime(new Date()); // 通过数据库触发器实现
        return scheduleTaskDefinitionMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(ScheduleTaskDefinitionExample example) {
        if (example == null) {
            return 0L;
        }

        return scheduleTaskDefinitionMapper.countByExample(example);
    }

    public List<ScheduleTaskDefinition> selectByExample(ScheduleTaskDefinitionExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return scheduleTaskDefinitionMapper.selectByExample(example);
    }

    // 深度分页的service接口
    public List<ScheduleTaskDefinition> selectByExampleDeepPaging(ScheduleTaskDefinitionExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        if (example.getLimit() == null || example.getOffset() == null){
            throw new IllegalArgumentException("limit or offset can't be null");
        }
        return scheduleTaskDefinitionMapper.selectByExampleDeepPagingByIdSubQuery(example);
    }

    // insertBatch的service接口
    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int createBatch(Collection<ScheduleTaskDefinition> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return scheduleTaskDefinitionMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int createBatchSelective(Collection<ScheduleTaskDefinition> records, Collection<String> excludeColNames) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return scheduleTaskDefinitionMapper.insertBatchSelective(records, new HashSet<String>(excludeColNames));
    }
    // 基于scheduleTaskDefinitionDao

}
