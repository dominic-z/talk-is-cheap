package cn.itcast.netty.c3;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;

@Slf4j
public class TestNettyPromise {

    @Test
    public void demo0() throws ExecutionException, InterruptedException {
        // 1. 准备 EventLoop 对象
        EventLoop eventLoop = new NioEventLoopGroup().next();
        // 2. 可以主动创建 promise, 结果容器
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);
        new Thread(() -> {
            // 3. 任意一个线程执行计算，计算完毕后向 promise 填充结果
            log.debug("开始计算...");
            try {
                Thread.sleep(2000);
                int i = 1 / 0;
                promise.setSuccess(80);
            } catch (Exception e) {
                e.printStackTrace();
                promise.setFailure(e);
            }

        }).start();
        // 4. 接收结果的线程
        log.debug("等待结果...");
//        log.debug("结果是: {}", promise.get());
        log.debug("异常是:", promise.cause());
    }


    @Test
    public void demo1() {
        DefaultEventLoop eventLoop = new DefaultEventLoop();
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        eventLoop.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("set success, {}", 10);
            promise.setSuccess(10);
        });

        log.debug("start...");
        log.debug("get now: {}", promise.getNow()); // 还没有结果
        try {
            log.debug("get down: {}", promise.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void demo2() {
        DefaultEventLoop eventLoop = new DefaultEventLoop();
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        promise.addListener(future -> {
            if (future.isSuccess()) {
                log.info("listener: promise success: get: {}", future.getNow());
            } else {
                log.error("listener: promise fail", future.cause());
            }
        });

        eventLoop.execute(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int i = new Random().nextInt(100);
            if (i % 2 == 0) {
                log.debug("set success, {}", i);
                promise.setSuccess(i);
            } else {
                log.debug("set fail, {}", i);
                promise.setFailure(new RuntimeException(i + ""));
            }
        });


        try {
            log.info("main done and wait for promise");
            promise.await();
            if (promise.isSuccess()) {
                log.info("sync: promise success: get: {}", promise.getNow());
            } else {
                log.error("sync: promise fail", promise.cause());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deadLock(){
        DefaultEventLoop eventExecutors = new DefaultEventLoop();
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventExecutors);

        eventExecutors.submit(()->{
            System.out.println("1");
            try {
                promise.await();
                // 注意不能仅捕获 InterruptedException 异常
                // 否则 死锁检查抛出的 BlockingOperationException 会继续向上传播
                // 而提交的任务会被包装为 PromiseTask，它的 run 方法中会 catch 所有异常然后设置为 Promise 的失败结果而不会抛出
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("2");
        });
    }
}
