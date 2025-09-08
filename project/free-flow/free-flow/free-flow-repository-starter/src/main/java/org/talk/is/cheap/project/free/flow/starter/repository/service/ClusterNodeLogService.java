package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.ClusterNodeLogMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.ClusterNodeLogDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNodeLog;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.ClusterNodeLogExample;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.lang.IllegalArgumentException;

/**
* 定制化的service层，用于弥补mbg生成的mapper过于灵活导致可能出现的业务漏洞，例如越过deleted字段查询、更新updateTime等
* @author dominiczhu
* @date 2025/09/07
*/
@Service
public class ClusterNodeLogService{

    @Autowired
    private ClusterNodeLogDao clusterNodeLogDao;

    @Autowired
    private ClusterNodeLogMapper clusterNodeLogMapper;

    // 基于ClusterNodeLogMapper

    @Transactional(rollbackFor = Exception.class ,transactionManager = "repositoryStarterTransactionManager")
    public int create(ClusterNodeLog record) {
        if (record == null) {
            return 0;
        }
        return clusterNodeLogMapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int createBatch(Collection<ClusterNodeLog> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return clusterNodeLogMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int deleteByExample(ClusterNodeLogExample example) {
        if (example == null) {
            return 0;
        }
        return clusterNodeLogMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class,transactionManager = "repositoryStarterTransactionManager")
    public int updateByExampleSelective(ClusterNodeLog record, ClusterNodeLogExample example) {
        if (record == null || example == null) {
            return 0;
        }
        // record.setUpdateTime(new Date()); // 通过数据库触发器实现
        return clusterNodeLogMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(ClusterNodeLogExample example) {
        if (example == null) {
            return 0L;
        }

        return clusterNodeLogMapper.countByExample(example);
    }

    public List<ClusterNodeLog> selectByExample(ClusterNodeLogExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return clusterNodeLogMapper.selectByExample(example);
    }

    // 深度分页的service接口
    public List<ClusterNodeLog> selectByExampleDeepPaging(ClusterNodeLogExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        if (example.getLimit() == null || example.getOffset() == null){
            throw new IllegalArgumentException("limit or offset can't be null");
        }
        return clusterNodeLogMapper.selectByExampleDeepPagingByIdSubQuery(example);
    }

    // 基于clusterNodeLogDao

}
