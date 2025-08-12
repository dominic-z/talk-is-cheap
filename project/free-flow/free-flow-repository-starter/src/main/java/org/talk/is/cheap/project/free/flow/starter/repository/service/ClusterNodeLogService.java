package org.talk.is.cheap.project.free.flow.starter.repository.service;

import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.ClusterNodeLogMapper;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.ClusterNodeLogDao;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.enums.NodeStatus;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.enums.NodeType;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNodeLog;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.query.example.ClusterNodeLogExample;

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
public class ClusterNodeLogService{

    @Autowired
    private ClusterNodeLogDao clusterNodeLogDao;

    @Autowired
    private ClusterNodeLogMapper clusterNodeLogMapper;

    // 基于ClusterNodeLogMapper
    @Transactional(rollbackFor = Exception.class ,transactionManager = "repositoryStarterTransactionManager")
    public void testTx() {
        ClusterNodeLog clusterNodeLog = new ClusterNodeLog();
        clusterNodeLog.setNodeId("10.1.1.1");
        clusterNodeLog.setNodeStatus(NodeStatus.QUIT_RUNNABLE.getStatus());
        clusterNodeLog.setNodeType(NodeType.SCHEDULER.getType());
        clusterNodeLogMapper.insertSelective(clusterNodeLog);
        int i=1/0;
    }
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
        record.setUpdateTime(new Date());
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

    // 基于clusterNodeLogDao

}
