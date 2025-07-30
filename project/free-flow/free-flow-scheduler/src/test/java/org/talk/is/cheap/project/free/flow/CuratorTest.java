package org.talk.is.cheap.project.free.flow;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.talk.is.cheap.project.free.flow.scheduler.App;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.dao.customized.ClusterNodeRegistryLogDao;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.dao.mbg.ClusterNodeRegistryLogMapper;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.pojo.ClusterNodeRegistryLog;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.query.example.ClusterNodeRegistryLogExample;

import javax.sql.DataSource;
import java.util.List;

@SpringBootTest(classes = App.class)
@Slf4j
public class CuratorTest {


    @Autowired
    CuratorFramework curatorFramework;

    @Autowired
    DataSource dataSource;


    @Autowired
    ClusterNodeRegistryLogMapper clusterNodeRegistryLogMapper;

    @Autowired
    ClusterNodeRegistryLogDao clusterNodeRegistryLogDao;

    @Test
    public void curd() throws Exception {
//        curatorFramework.create()
//                .creatingParentsIfNeeded()
//                .withMode(CreateMode.PERSISTENT)
////                .withACL()
//                .forPath("/ahao1/test","this is a book".getBytes());

        log.info("{}",dataSource);


//        clusterNodeRegistryLogMapper.insertSelective(new ClusterNodeRegistryLog().withNodeId("1").withNodeStatus(2).withNodeType(2));
        List<ClusterNodeRegistryLog> clusterNodeRegistryLogs =
                clusterNodeRegistryLogMapper.selectByExample(new ClusterNodeRegistryLogExample());
        log.info("{}",clusterNodeRegistryLogs);
        log.info("{}",clusterNodeRegistryLogDao.select());
    }
}
