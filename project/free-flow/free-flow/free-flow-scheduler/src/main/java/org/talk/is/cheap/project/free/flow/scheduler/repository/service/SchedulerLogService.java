package org.talk.is.cheap.project.free.flow.scheduler.repository.service;

import org.talk.is.cheap.project.free.flow.scheduler.repository.dao.mbg.SchedulerLogMapper;
import org.talk.is.cheap.project.free.flow.scheduler.repository.dao.customized.SchedulerLogDao;
import org.talk.is.cheap.project.free.flow.scheduler.repository.domain.pojo.SchedulerLog;
import org.talk.is.cheap.project.free.flow.scheduler.repository.domain.query.example.SchedulerLogExample;

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
public class SchedulerLogService{

    @Autowired
    private SchedulerLogDao schedulerLogDao;

    @Autowired
    private SchedulerLogMapper schedulerLogMapper;

    // 基于SchedulerLogMapper

    @Transactional(rollbackFor = Exception.class)
    public int create(SchedulerLog record) {
        if (record == null) {
            return 0;
        }
        return schedulerLogMapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public int createBatch(Collection<SchedulerLog> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return schedulerLogMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByExample(SchedulerLogExample example) {
        if (example == null) {
            return 0;
        }
        return schedulerLogMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByExampleSelective(SchedulerLog record, SchedulerLogExample example) {
        if (record == null || example == null) {
            return 0;
        }
        record.setUpdateTime(new Date());
        return schedulerLogMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(SchedulerLogExample example) {
        if (example == null) {
            return 0L;
        }

        return schedulerLogMapper.countByExample(example);
    }

    public List<SchedulerLog> selectByExample(SchedulerLogExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return schedulerLogMapper.selectByExample(example);
    }

    // 基于schedulerLogDao

}
