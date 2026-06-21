package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.ClusterNodeMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.ClusterNodeDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNode;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.ClusterNodeExample;

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
* @date 2025/12/25
*/
@Service
public class ClusterNodeService{

    @Autowired
    private ClusterNodeDao clusterNodeDao;

    @Autowired
    private ClusterNodeMapper clusterNodeMapper;

    // 基于ClusterNodeMapper

    @Transactional(rollbackFor = Exception.class , transactionManager = "repositoryStarterTransactionManager")
    public int create(ClusterNode record) {
        if (record == null) {
            return 0;
        }
        return clusterNodeMapper.insertSelective(record);
    }


    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int deleteByExample(ClusterNodeExample example) {
        if (example == null) {
            return 0;
        }
        return clusterNodeMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int updateByExampleSelective(ClusterNode record, ClusterNodeExample example) {
        if (record == null || example == null) {
            return 0;
        }
        // record.setUpdateTime(new Date()); // 通过数据库触发器实现
        return clusterNodeMapper.updateByExampleSelective(record, example);
    }

    public long countByExample(ClusterNodeExample example) {
        if (example == null) {
            return 0L;
        }

        return clusterNodeMapper.countByExample(example);
    }

    public List<ClusterNode> selectByExample(ClusterNodeExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return clusterNodeMapper.selectByExample(example);
    }

    // 深度分页的service接口
    public List<ClusterNode> selectByExampleDeepPaging(ClusterNodeExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        if (example.getLimit() == null || example.getOffset() == null){
            throw new IllegalArgumentException("limit or offset can't be null");
        }
        return clusterNodeMapper.selectByExampleDeepPagingByIdSubQuery(example);
    }

    // insertBatch的service接口
    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int createBatch(Collection<ClusterNode> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return clusterNodeMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "repositoryStarterTransactionManager")
    public int createBatchSelective(Collection<ClusterNode> records, Collection<String> excludeColNames) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return clusterNodeMapper.insertBatchSelective(records, new HashSet<String>(excludeColNames));
    }
    // 基于clusterNodeDao
    @Transactional(rollbackFor = Exception.class , transactionManager = "repositoryStarterTransactionManager")
    public int createOnDuplicateKey(ClusterNode record) {
        if (record == null) {
            return 0;
        }
        return clusterNodeDao.insertOnDuplicateKey(record);
    }
}
