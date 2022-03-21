package section3.chapter30;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ThreadId
 * @date 2021/7/27 上午9:41
 */
public class ThreadId {
    static final AtomicLong
            nextId = new AtomicLong(0);
    //定义ThreadLocal变量
    static final ThreadLocal<Long>
            tl = ThreadLocal.withInitial(
            () -> nextId.getAndIncrement());

    //此方法会为每个线程分配一个唯一的Id
    static long get() {
        return tl.get();
    }

    public static void main(String[] args) throws InterruptedException {
//        ExecutorService executorService = Executors.newFixedThreadPool(2);
//
//        executorService.submit(()->{
//            System.out.println(Thread.currentThread().toString()+ThreadId.get());
//            System.out.println(Thread.currentThread().toString()+ThreadId.get());
//        });
//
//        executorService.submit(()->{
//            System.out.println(Thread.currentThread().toString()+ThreadId.get());
//            System.out.println(Thread.currentThread().toString()+ThreadId.get());
//        });

//        executorService.shutdown();
//        executorService.awaitTermination(30, TimeUnit.SECONDS);

        Thread thread1 = new Thread(()->{
            System.out.println(ThreadId.get());
        });
//
//        Thread thread2 = new Thread(()->{
//            System.out.println(ThreadId.get());
//        });

        thread1.start();
//        thread2.start();


    }
}
