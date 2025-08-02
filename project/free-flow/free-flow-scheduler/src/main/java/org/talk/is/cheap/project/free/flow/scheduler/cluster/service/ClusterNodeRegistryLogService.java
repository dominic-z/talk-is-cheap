package org.talk.is.cheap.project.free.flow.scheduler.cluster.service;

import org.talk.is.cheap.project.free.flow.scheduler.cluster.dao.mbg.ClusterNodeRegistryLogMapper;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.dao.customized.ClusterNodeRegistryLogDao;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.pojo.ClusterNodeRegistryLog;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.query.example.ClusterNodeRegistryLogExample;

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
* @date 2025/08/02
*/
@Service
public class ClusterNodeRegistryLogService{

    @Autowired
    private ClusterNodeRegistryLogDao clusterNodeRegistryLogDao;

    @Autowired
    private ClusterNodeRegistryLogMapper clusterNodeRegistryLogMapper;

    // 基于ClusterNodeRegistryLogMapper

    @Transactional(rollbackFor = Exception.class)
    public int create(ClusterNodeRegistryLog record) {
        if (record == null) {
            return 0;
        }
        return clusterNodeRegistryLogMapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public int createBatch(Collection<ClusterNodeRegistryLog> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return clusterNodeRegistryLogMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByExample(ClusterNodeRegistryLogExample example) {
        if (example == null) {
            return 0;
        }
        return clusterNodeRegistryLogMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByExampleSelective(ClusterNodeRegistryLog record, ClusterNodeRegistryLogExample example) {
        if (record == null || example == null) {
            return 0;
        }
        record.setUpdateTime(new Date());
        return clusterNodeRegistryLogMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(ClusterNodeRegistryLogExample example) {
        if (example == null) {
            return 0L;
        }

        return clusterNodeRegistryLogMapper.countByExample(example);
    }

    public List<ClusterNodeRegistryLog> selectByExample(ClusterNodeRegistryLogExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return clusterNodeRegistryLogMapper.selectByExample(example);
    }

    // 基于clusterNodeRegistryLogDao

}
