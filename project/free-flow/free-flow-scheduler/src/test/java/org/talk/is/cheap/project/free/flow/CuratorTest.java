package org.talk.is.cheap.project.free.flow;


import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.talk.is.cheap.project.free.flow.scheduler.App;

@SpringBootTest(classes = App.class)
public class CuratorTest {


    @Autowired
    CuratorFramework curatorFramework;

    @Test
    public void curd() throws Exception {
//        curatorFramework.create()
//                .creatingParentsIfNeeded()
//                .withMode(CreateMode.PERSISTENT)
////                .withACL()
//                .forPath("/ahao1/test","this is a book".getBytes());


    }
}
