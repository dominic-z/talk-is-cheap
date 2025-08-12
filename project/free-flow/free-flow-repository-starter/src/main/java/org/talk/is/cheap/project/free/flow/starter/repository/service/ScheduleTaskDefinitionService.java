package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.ScheduleTaskDefinitionMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.ScheduleTaskDefinitionDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ScheduleTaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.query.example.ScheduleTaskDefinitionExample;

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
* @date 2025/08/12
*/
@Service
public class ScheduleTaskDefinitionService{

    @Autowired
    private ScheduleTaskDefinitionDao scheduleTaskDefinitionDao;

    @Autowired
    private ScheduleTaskDefinitionMapper scheduleTaskDefinitionMapper;

    // 基于ScheduleTaskDefinitionMapper

    @Transactional(rollbackFor = Exception.class ,transactionManager = "repositoryStarterTransactionManager")
    public int create(ScheduleTaskDefinition record) {
        if (record == null) {
            return 0;
        }
        return scheduleTaskDefinitionMapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int createBatch(Collection<ScheduleTaskDefinition> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return scheduleTaskDefinitionMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int deleteByExample(ScheduleTaskDefinitionExample example) {
        if (example == null) {
            return 0;
        }
        return scheduleTaskDefinitionMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int updateByExampleSelective(ScheduleTaskDefinition record, ScheduleTaskDefinitionExample example) {
        if (record == null || example == null) {
            return 0;
        }
        record.setUpdateTime(new Date());
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

    // 基于scheduleTaskDefinitionDao

}
