package org.talk.is.cheap.project.free.flow;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.talk.is.cheap.project.free.flow.scheduler.App;
import org.talk.is.cheap.project.free.flow.scheduler.repository.dao.mbg.SchedulerLogMapper;
import org.talk.is.cheap.project.free.flow.scheduler.repository.domain.query.example.SchedulerLogExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNodeLog;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.query.example.ClusterNodeLogExample;
import org.talk.is.cheap.project.free.flow.starter.repository.service.ClusterNodeLogService;

import javax.sql.DataSource;
import java.util.List;

@SpringBootTest(classes = App.class)
@Slf4j
public class TestSpringApp {


    @Autowired
    CuratorFramework curatorFramework;

    @Autowired
    DataSource dataSource;

    @Autowired
    private ClusterNodeLogService clusterNodeLogService;

    @Autowired
    private SchedulerLogMapper schedulerLogMapper;


    @Test
    public void curd() throws Exception {
//        curatorFramework.create()
//                .creatingParentsIfNeeded()
//                .withMode(CreateMode.PERSISTENT)
////                .withACL()
//                .forPath("/ahao1/test","this is a book".getBytes());

        log.info("{}",dataSource);


        try {
            clusterNodeLogService.testTx();
        }catch (Exception e){
            log.error("error",e);
        }

        ClusterNodeLogExample query = new ClusterNodeLogExample();
        query.createCriteria().andIdGreaterThan(0L);
        List<ClusterNodeLog> clusterNodeLogs = clusterNodeLogService.selectByExample(query);
        log.info("{}",clusterNodeLogs);
//        log.info("{}",clusterNodeRegistryLogDao.s());


        SchedulerLogExample schedulerLogExample = new SchedulerLogExample();
        schedulerLogExample.createCriteria().andIdEqualTo(1L);
        log.info("{}",schedulerLogMapper.selectByExample(schedulerLogExample));
    }



}
