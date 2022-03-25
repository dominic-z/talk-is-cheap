package com.atguigu.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

@Slf4j
public class zkClient {

    // 注意：逗号左右不能有空格
    private String connectString = "localhost:2181,localhost:2182,localhost:2183";
    private int sessionTimeout = 2000;
    private ZooKeeper zkClient;

    @Before
    public void init() throws IOException {

        zkClient = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {

                log.info("watchedEvent {}", watchedEvent);
                System.out.println("-------------------------------");
                List<String> children = null;
                try {
                    children = zkClient.getChildren("/", true);

                    for (String child : children) {
                        log.info("watcher {}", child);
                    }

                    System.out.println("-------------------------------");
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Test
    public void create() throws KeeperException, InterruptedException {
        String nodeCreated = zkClient.create("/atguigu", "ss.avi".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        log.info("nodeCreated {}", nodeCreated);
    }

    @Test
    public void getChildren() throws KeeperException, InterruptedException {
        List<String> children = zkClient.getChildren("/", true);

        for (String child : children) {
            log.info("getChildren {}", child);
        }

        // 延时
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void exist() throws KeeperException, InterruptedException {

        Stat stat = zkClient.exists("/atguigu", false);
        log.info("stat {}", stat);
    }
}
