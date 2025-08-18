package org.talk.is.cheap.project.free.flow;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.talk.is.cheap.project.free.flow.scheduler.App;
import org.talk.is.cheap.project.free.flow.scheduler.repository.dao.mbg.SchedulerLogMapper;
import org.talk.is.cheap.project.free.flow.scheduler.repository.domain.query.example.SchedulerLogExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.TaskStartupParam;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNodeLog;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.query.example.ClusterNodeLogExample;
import org.talk.is.cheap.project.free.flow.starter.repository.service.ClusterNodeLogService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.SeqGeneratorUtil;
import org.talk.is.cheap.project.free.flow.starter.repository.service.es.TaskStartupParamService;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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

        log.info("{}", dataSource);


        try {
//            clusterNodeLogService.testTx();
        } catch (Exception e) {
            log.error("error", e);
        }

        ClusterNodeLogExample query = new ClusterNodeLogExample();
        query.createCriteria().andIdGreaterThan(0L);
        List<ClusterNodeLog> clusterNodeLogs = clusterNodeLogService.selectByExample(query);
        log.info("{}", clusterNodeLogs);
//        log.info("{}",clusterNodeRegistryLogDao.s());


        SchedulerLogExample schedulerLogExample = new SchedulerLogExample();
        schedulerLogExample.createCriteria().andIdEqualTo(1L);
        log.info("{}", schedulerLogMapper.selectByExample(schedulerLogExample));
    }


    @Autowired
    private SeqGeneratorUtil seqGeneratorUtil;


    /**
     * 测试序列生成器的并发安全性
     * allow mutlti instance模拟多个机器，然后把step调整的小一点，加大并发冲突
     */
    @Test
    public void testConcurrentGenerator() {
        List<Thread> threads = new ArrayList<>();
        List<List<Long>> idlists1 = new ArrayList<>();
        List<List<Long>> idlists2 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final List<Long> ids1 = new ArrayList<>();
            idlists1.add(ids1);
            Thread thread1 = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    long nextId = seqGeneratorUtil.getNextId("task_startup_param");
                    ids1.add(nextId);
                }
            });

            threads.add(thread1);
            thread1.start();


            final List<Long> ids2 = new ArrayList<>();
            idlists2.add(ids2);
            Thread thread2 = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    long nextId = seqGeneratorUtil.getNextId("stage_execution_biz_log");
                    ids2.add(nextId);
                }
            });
            threads.add(thread2);
            thread2.start();
        }

        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        HashSet<Long> idSet = new HashSet<>();
        idlists1.stream().flatMap(ids -> ids.stream()).forEach(id -> {
            if (idSet.contains(id)) {
                System.out.println(id + " 重复");
            }
            idSet.add(id);
        });
        // 应该是100*1000 10万 数据库里刚好也应该是10万
        System.out.println(idSet.size());


        idSet.clear();
        idlists2.stream().flatMap(ids -> ids.stream()).forEach(id -> {
            if (idSet.contains(id)) {
                System.out.println(id + " 重复");
            }
            idSet.add(id);
        });
        // 应该是100*1000 10万 数据库里刚好也应该是10万
        System.out.println(idSet.size());

    }


    @Autowired
    private TaskStartupParamService taskStartupParamService;

    @Test
    public void testEs() throws IOException {
        TaskStartupParam taskStartupParam = new TaskStartupParam();
        taskStartupParam.setTaskStartupId(12L);
        taskStartupParam.setStartupParamFullyQualifiedClassName("a.b.c");
        taskStartupParam.setStartupParamEncoding("cccc");

        String id = taskStartupParamService.create(taskStartupParam);
        log.info("create id: {}", id);
    }
}
