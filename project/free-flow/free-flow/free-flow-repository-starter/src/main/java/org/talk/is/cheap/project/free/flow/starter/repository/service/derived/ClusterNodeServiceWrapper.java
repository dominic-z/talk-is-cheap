package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.ClusterNodeExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskStartupExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNode;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.service.ClusterNodeService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskStartupService;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ClusterNodeServiceWrapper {

    @Autowired
    private ClusterNodeService clusterNodeService;


    public ClusterNode selectById(long id, int... status) {
        ClusterNodeExample clusterNodeExample = new ClusterNodeExample();
        ClusterNodeExample.Criteria criteria = clusterNodeExample.createCriteria();
        criteria.andIdEqualTo(id);

        if (status != null && status.length != 0) {
            criteria.andStatusIn(Arrays.stream(status).boxed().toList());
        }

        List<ClusterNode> clusterNodes = clusterNodeService.selectByExample(clusterNodeExample);
        if (clusterNodes == null || clusterNodes.isEmpty()) {
            return null;
        }
        return clusterNodes.get(0);
    }


    public ClusterNode selectByAddress(String address, int... status) {
        ClusterNodeExample clusterNodeExample = new ClusterNodeExample();
        ClusterNodeExample.Criteria criteria = clusterNodeExample.createCriteria();
        criteria.andNodeAddressEqualTo(address);

        if (status != null && status.length != 0) {
            criteria.andStatusIn(Arrays.stream(status).boxed().toList());
        }

        List<ClusterNode> clusterNodes = clusterNodeService.selectByExample(clusterNodeExample);
        if (clusterNodes == null || clusterNodes.isEmpty()) {
            return null;
        }
        return clusterNodes.get(0);
    }


    public int updateByIdSelective(long id, ClusterNode clusterNode, Long revision) {
        ClusterNodeExample clusterNodeExample = new ClusterNodeExample();
        ClusterNodeExample.Criteria criteria = clusterNodeExample.createCriteria();
        criteria.andIdEqualTo(id);
        if (revision != null) {
            criteria.andRevisionEqualTo(revision);
            clusterNode.setRevision(revision + 1);
        }

        return clusterNodeService.updateByExampleSelective(clusterNode, clusterNodeExample);
    }


    public int updateByNodeAddressSelective(String nodeAddress, ClusterNode clusterNode) {
        ClusterNodeExample clusterNodeExample = new ClusterNodeExample();
        ClusterNodeExample.Criteria criteria = clusterNodeExample.createCriteria();
        criteria.andNodeAddressEqualTo(nodeAddress);

        return clusterNodeService.updateByExampleSelective(clusterNode, clusterNodeExample);
    }

    public int updateByNodeAddressAndStatusSelective(String nodeAddress, Integer status, ClusterNode clusterNode) {
        ClusterNodeExample clusterNodeExample = new ClusterNodeExample();
        ClusterNodeExample.Criteria criteria = clusterNodeExample.createCriteria();
        criteria.andNodeAddressEqualTo(nodeAddress);
        if (status != null) {
            criteria.andStatusEqualTo(status);
        }

        return clusterNodeService.updateByExampleSelective(clusterNode, clusterNodeExample);
    }
}
