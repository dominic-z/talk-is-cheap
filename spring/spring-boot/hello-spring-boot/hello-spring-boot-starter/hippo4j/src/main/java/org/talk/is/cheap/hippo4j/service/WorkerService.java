package org.talk.is.cheap.hippo4j.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class WorkerService {


    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private ThreadPoolExecutor messageConsumeDynamicExecutor;


    @Autowired
    private ThreadPoolExecutor messageProduceDynamicExecutor;

    public void doTask(int runCount){

        messageConsumeDynamicExecutor.execute(()->{
            int count = 1;

            while (count<=runCount){
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
//                log.info("do work");
            }
        });
    }


    public void logPool(){
        log.info("core: {}, max: {}",messageConsumeDynamicExecutor.getCorePoolSize(),messageConsumeDynamicExecutor.getMaximumPoolSize());
        log.info("queue class: {}, queue size: {}",messageConsumeDynamicExecutor.getQueue().getClass(),messageConsumeDynamicExecutor.getQueue().remainingCapacity());
    }
}
