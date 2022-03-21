package com.csdn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author dominiczhu
 * @date 2020/9/3 9:15 上午
 */

public class ScheduleDemo {
    private static Logger log = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    private String getStringSync() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Hello, Reactor!";
    }

    @Test
    public void testSyncToAsync() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        Mono.fromCallable(this::getStringSync)    // 1
                .subscribeOn(Schedulers.elastic())  // 2
                .subscribe((s)->{
                    log.info("Publish {}, {}", Thread.currentThread(), s);
                }, null, countDownLatch::countDown);

//        Flux.range(1, 1000)
//                .map(…)
//                .publishOn(Schedulers.elastic()).filter(…) 影响这个filter
//                .publishOn(Schedulers.parallel()).flatMap(…) 影响这个flatMap
//                .subscribeOn(Schedulers.single()) //影响第一个map
//         在流中有发布者与订阅者，但是个人理解，一个环节有可能即是订阅者也是发布者，比如上面的注释这个例子里的map，他对于上游的range来说，他是订阅者，但是对于下游的filter来说，它又是发布者，所以subscribeOn影响了map的订阅，儿第一个publish影响了第一个map向后的发布

        //下面是我从geekbang的教程里抄过来的，根据currentThread可以看出，上面的Schedulers.elastic()运行在elastic-2,5,main，而下面的代码运行在elastic-3,5,main，这是一个新的线程
        Flux.range(1, 6)
                .doOnRequest(n -> log.info("Request {} number", n)) // 注意顺序造成的区别
                .doOnComplete(() -> log.info("Publisher COMPLETE 1"))
                .publishOn(Schedulers.elastic())
                .map(i -> {
                    log.info("Publish {}, {}", Thread.currentThread(), i);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return i;
                })
                .doOnComplete(() -> log.info("Publisher COMPLETE 2"))
                .subscribeOn(Schedulers.single())
                .subscribe(i -> log.info("Subscribe {}: {}", Thread.currentThread(), i),
                        e -> log.error("error {}", e.toString()),
                        () -> {log.info("Subscriber COMPLETE");countDownLatch.countDown();}//,
//						s -> s.request(4)
                );


        countDownLatch.await(20, TimeUnit.SECONDS);
    }


}
