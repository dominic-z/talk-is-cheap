package org.talk.is.cheap.project.free.flow.scheduler.cluster.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * 负责管理worker集群
 */
@Service
@Slf4j
public class WorkerClusterManager {


    @Autowired
    private CuratorFramework curatorZKClient;


    @Value("${apache.zookeeper.path.worker}")
    private String zkWorkerPath;

    /**
     * 监听worker
     */
    public void watchWorker(){

        CuratorCacheListener listener = CuratorCacheListener.builder().forTreeCache(curatorZKClient, new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                TreeCacheEvent.Type type = event.getType();
                switch (type){
                    case NODE_ADDED:
                        handleAddWorker(event);
                        break;
                    case NODE_REMOVED:
                        handleRemoveWorker(event);
                        break;
                    default:
                }
            }
        }).build();

        // todo: 没有关闭
        CuratorCache curatorCache = CuratorCache.builder(curatorZKClient, zkWorkerPath).build();
        curatorCache.listenable().addListener(listener);
        curatorCache.start();
    }

    private void handleAddWorker(TreeCacheEvent event){
        if(event.getData()!=null && !StringUtils.equals(event.getData().getPath(),zkWorkerPath)){
            // 只监控zkWorkerPath下的节点，对zkWorkerPath不关注
            log.info("add worker, path: {}, workerId: {}",event.getData().getPath(),new String(event.getData().getData(), StandardCharsets.UTF_8));
        }
    }

    private void handleRemoveWorker(TreeCacheEvent event){
        log.info("delete worker, path: {}, workerId: {}",event.getData().getPath(),new String(event.getData().getData(), StandardCharsets.UTF_8));
    }

}
