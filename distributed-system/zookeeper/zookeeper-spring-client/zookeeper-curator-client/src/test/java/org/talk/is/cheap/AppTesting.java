package org.talk.is.cheap;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.leader.*;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(classes = App.class)
@Slf4j
public class AppTesting {
    @Autowired
    CuratorFramework adminCuratorClient;

    @Autowired
    CuratorFramework tenant1CuratorClient;

    /**
     * 实验之前记得去zk里把路径删除从头开始测试
     *
     * @throws Exception
     */
    @Test
    public void curd() throws Exception {
        final String path = "/tenant1/tt";
        //        清空 以便后续处理
        if (tenant1CuratorClient.checkExists().forPath(path) != null) {
            tenant1CuratorClient.delete().deletingChildrenIfNeeded().forPath(path);
        }
//        新增

        // todo: 20250728惊奇的发现，如果createMode是临时节点，并且还使用debug卡在某个地方的时候，时间稍微长一点，这个临时节点就会被自动删掉，怀疑是zk有机制，维持临时节点需要客户端持续心跳。
        String createPath = tenant1CuratorClient.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(path, "this is a book".getBytes());
        log.info("create result: {}", createPath);

        log.info("get data: {}", new String(tenant1CuratorClient.getData().forPath(path), StandardCharsets.UTF_8));
        log.info("get children: {}", tenant1CuratorClient.getChildren().forPath("/tenant1"));
//        修改
        tenant1CuratorClient.setData()
                .forPath(path, "this is a new book".getBytes());
//        查询
        log.info("get data: {}", new String(tenant1CuratorClient.getData().forPath(path), StandardCharsets.UTF_8));

//      异步修改
        tenant1CuratorClient.setData().inBackground(new BackgroundCallback() {
            @Override
            public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                log.info("set done {}", curatorEvent);
                log.info("get data: {}", new String(tenant1CuratorClient.getData().forPath(path), StandardCharsets.UTF_8));
            }
        }).forPath(path, "this is a newest book".getBytes());

//        删除
        log.info("path exist: {}", tenant1CuratorClient.checkExists().forPath(path));
        tenant1CuratorClient.delete().forPath(path);
        log.info("path exist: {}", tenant1CuratorClient.checkExists().forPath(path));

    }


    /**
     * 监听
     *
     * 一些奇怪的笔记：
     * 1. 如果我通过
     * @throws Exception
     */
    @Test
    public void testWatch() throws Exception {
        final String path = "/tenant1/watch";
        final String subPath = "/tenant1/watch/sub";
        final String subSubPath = "/tenant1/watch/sub/sub";
        // 如果执行清空，那么后续监听的时候，第一个事件就是初始化，第二个事件应该就是创建
        // 而如果不执行清空，并且path/subPath/subSubPath已经存在的话，那么第一个事件就是NODE_ADD，为啥？我推测既然这个监听器叫做什么什么cache，那么我可以理解为是对zk在本地做了个缓存，zk有而本地没有，那么就会触发node_add事件，
        // 相当于对于当前这个客户端来说，zk新增了节点，好像也合理。
        if (tenant1CuratorClient.checkExists().forPath(path) != null) {
            tenant1CuratorClient.delete().deletingChildrenIfNeeded().forPath(path);
        }
        if (tenant1CuratorClient.checkExists().forPath(subPath) != null) {
            tenant1CuratorClient.delete().deletingChildrenIfNeeded().forPath(subPath);
        }
        if (tenant1CuratorClient.checkExists().forPath(subSubPath) != null) {
            tenant1CuratorClient.delete().deletingChildrenIfNeeded().forPath(subSubPath);
        }
        // 监听一个节点
        // 创建NodeCache对象
        // deprecated了
//        NodeCache nodeCache = new NodeCache(tenant1CuratorClient,path);
        // 添加监听器
//        nodeCache.getListenable().addListener(new NodeCacheListener() {
//            @Override
//            public void nodeChanged() throws Exception {
//                ChildData currentData = nodeCache.getCurrentData();
//                if (currentData != null){
//                    String s = new String(currentData.getData(),StandardCharsets.UTF_8);
//                    log.info("监听{}节点发生变化，数据内容：{}",path,s);
//                }else {
//                    log.info("监听{}节点被删除了",path);
//                }
//            }
//        });
//        // 开启监听
//        nodeCache.start();
//
//        TimeUnit.SECONDS.sleep(2);
//        // 创建节点
//        client.create().creatingParentsIfNeeded().forPath(path,"第一次新增".getBytes(StandardCharsets.UTF_8));
//        TimeUnit.SECONDS.sleep(2);
//        // 更新节点
//        client.setData().forPath(path,"数据修改了".getBytes(StandardCharsets.UTF_8));
//        TimeUnit.SECONDS.sleep(2);
//        // 删除节点
//        client.delete().deletingChildrenIfNeeded().forPath(path);


//        /**
//         * 缓存构建选项
//         */
//        enum Options
//        {
//            /**
//             * 通常，以指定节点为根的整个子树都会被缓存（默认缓存方案）
//             * 这个选项只会缓存指定的节点（即单节点缓存）
//             */
//            SINGLE_NODE_CACHE,
//
//            /**
//             * 通过org.apache.curator.framework.api.GetDataBuilder.decompressed()解压数据
//             */
//            COMPRESSED_DATA,
//
//            /**
//             * 通常，当通过close()关闭缓存时，会通过CuratorCacheStorage.clear()清除storage
//             * 此选项可防止清除storage
//             */
//            DO_NOT_CLEAR_ON_CLOSE
//        }
//————————————————
//
//        版权声明：本文为博主原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接和本声明。
//
//        原文链接：https://blog.csdn.net/qq_37960603/article/details/121835169
        CuratorCache curatorCache = CuratorCache.builder(tenant1CuratorClient, path).build();
        CuratorCacheListener listener = CuratorCacheListener.builder()
//                .forNodeCache(new NodeCacheListener() {
//                    @Override
//                    public void nodeChanged() throws Exception {
//                        log.info("node changed");
//                    }
//                })
                .forCreates(childData -> {
                    log.info("path: {}, data: {}", childData.getPath(), new String(childData.getData(), StandardCharsets.UTF_8));
                })
                .forChanges(new CuratorCacheListenerBuilder.ChangeListener() {
                    @Override
                    public void event(ChildData oldNode, ChildData node) {
                        log.info("value change: old path: {}, old data: {}, new path: {}, new data: {}",
                                oldNode.getPath(), new String(oldNode.getData(), StandardCharsets.UTF_8),
                                node.getPath(), new String(node.getData(), StandardCharsets.UTF_8));
                    }
                })
//                监听子节点
                .forPathChildrenCache(path, tenant1CuratorClient, new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                        if (event.getData() != null) {

                            log.info("child event: {},path: {},data:{}", event.getType(), event.getData().getPath(),
                                    new String(event.getData().getData()));
                        } else {
                            log.info("child event: {}", event.getType());

                        }
                    }
                })
//                监听当前节点以及所有下面的节点
                .forTreeCache(tenant1CuratorClient, new TreeCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                        if (event.getData() != null) {

                            log.info("tree child event: {},path: {},data:{}", event.getType(), event.getData().getPath(),
                                    new String(event.getData().getData()));
                        } else {
                            log.info("tree child event: {}", event.getType());

                        }
                    }
                })
                .build();

        curatorCache.listenable().addListener(listener);
        curatorCache.start();

//        新增
        tenant1CuratorClient.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(path, "this is a book".getBytes());
        //        修改
        tenant1CuratorClient.setData()
                .forPath(path, "this is a new book".getBytes());
        tenant1CuratorClient.delete().deletingChildrenIfNeeded().forPath(path);

        tenant1CuratorClient.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(subPath, "this is a book".getBytes());
        tenant1CuratorClient.setData()
                .forPath(path, "this is a new book".getBytes());
        tenant1CuratorClient.delete().deletingChildrenIfNeeded().forPath(subPath);

        tenant1CuratorClient.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(subSubPath, "this is a book".getBytes());
        tenant1CuratorClient.setData()
                .forPath(subSubPath, "this is a new book".getBytes());
        tenant1CuratorClient.delete().deletingChildrenIfNeeded().forPath(subSubPath);


    }


    /**
     * 本质上就是创建一些临时节点
     *
     * @throws Exception
     */
    @Test
    public void testLeaderLatch() throws Exception {
        String path = "/tenant1/test/leader";
//        if(tenant1CuratorClient.checkExists().forPath(path)!=null){
//            tenant1CuratorClient.delete().forPath(path);
//        }

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 5; i++) {
            String leaderName = "leader-" + i;
            Runnable candidate = new Runnable() {
                @Override
                public void run() {

                    // closemode控制当前latch在close的时候会不会通知listener
                    final LeaderLatch leaderLatch = new LeaderLatch(tenant1CuratorClient, path, leaderName,
                            LeaderLatch.CloseMode.NOTIFY_LEADER);
                    leaderLatch.addListener(new LeaderLatchListener() {
                        @Override
                        public void isLeader() {
                            log.info("!!!!!id: {} is leader, 1000ms之后释放leader权", leaderName);
                            try {
                                leaderLatch.getParticipants().forEach(participant -> log.info("查看当前的候选人：participant: {}", participant));
                                Thread.sleep(new Random().nextInt(1000));
                                leaderLatch.close();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public void notLeader() {
                            // 节点调用了close方法，只有在LeaderLatch.CloseMode.NOTIFY_LEADER模式下会调用该方法
                            // LeaderLatch.CloseMode.SILENT模式下不会调用该方法
                            log.info("!!!!!id: {} is not leader", leaderName);
                        }
                    });
                    log.info("{} 准备好了，短暂等待", leaderName);
                    try {
                        Thread.sleep(new Random().nextInt(100));
                        log.info("{} 开始进入选举", leaderName);
                        leaderLatch.start();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
//            LeaderLatch类不能close()多次，LeaderLatch.hasLeadership()与LeaderLatch.getLeader()得到的结果不一定一致，需要通过LeaderLatch.getLeader().isLeader
//            ()来判断。
            executorService.execute(candidate);
        }

        Thread.sleep(100000);

    }

    @Builder
    static class MyLeaderSelectorListener extends LeaderSelectorListenerAdapter {

        @Setter
        private String leaderName;
        @Setter
        private LeaderSelector leaderSelector;

        @Override
        public void takeLeadership(CuratorFramework client) throws Exception {
            log.info("!!!!!id: {} is leader, 一段时间将自动释放leader权", leaderName);
            leaderSelector.getParticipants().forEach(participant -> log.info("查看当前的候选人：participant: {}", participant));

            Thread.sleep(new Random().nextInt(1000));
        }
    }

    /**
     * @throws Exception
     */
    @Test
    public void testLeaderSelection() throws Exception {


        String path = "/tenant1/leader";
//        if(tenant1CuratorClient.checkExists().forPath(path)!=null){
//            tenant1CuratorClient.delete().forPath(path);
//        }

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            String leaderName = "leader:" + i;
            Runnable candidate = new Runnable() {
                @Override
                public void run() {
                    MyLeaderSelectorListener listener = MyLeaderSelectorListener.builder().leaderName(leaderName).build();
                    LeaderSelector leaderSelector = new LeaderSelector(tenant1CuratorClient, path, listener);
                    listener.setLeaderSelector(leaderSelector);

                    // takeLeadershoip执行完成后会自动重新排队
//                    leaderSelector.autoRequeue();

                    log.info("{} 准备好了，短暂等待", leaderName);
                    try {
                        Thread.sleep(new Random().nextInt(100));
                        log.info("{} 开始进入选举", leaderName);
                        leaderSelector.start();

                        Thread.sleep(5000);
//                        手动重新排队
                        leaderSelector.requeue();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            executorService.execute(candidate);
        }


        Thread.sleep(120000);
    }

}
