package concurrent.lock;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class AQSExample {

    @Test
    public void samphoreTest(){

    }

    @Test
    public void countDownLatchTest(){
        int threadCount = 3;
        // 创建一个 CountDownLatch 实例，指定需要等待完成的任务数量为 3
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int taskId = i;
            new Thread(() -> {
                try {
                    System.out.println("Task " + taskId + " is starting.");
                    // 模拟任务执行
                    Thread.sleep((long) (Math.random() * 1000));
                    System.out.println("Task " + taskId + " is completed.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // 任务完成后，计数器减 1
                    latch.countDown();
                }
            }).start();
        }

        try {
            // 主线程等待所有子线程完成任务
            latch.await();
            System.out.println("All tasks are completed. Main thread can continue.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void CyclicBarrierExample() throws InterruptedException {

        // 创建一个 CyclicBarrier 实例，指定需要等待的线程数量为 3
        CyclicBarrier barrier = new CyclicBarrier(3, () -> {
            System.out.println("All threads have reached the barrier. Starting the next phase.");
        });

        List<Thread> threadList = new ArrayList<>();
        // 创建并启动三个线程
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(() -> {
                {
                    try {
                        System.out.println("Thread " + Thread.currentThread() + " is working.");
                        // 模拟线程的工作
                        Thread.sleep((long) (Math.random() * 1000));
                        System.out.println("Thread " + Thread.currentThread() + " has reached the barrier.");
                        // 调用 await() 方法等待其他线程
                        barrier.await();
                        System.out.println("Thread " + Thread.currentThread() + " continues after the barrier.");
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            });
            threadList.add(thread);
            thread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }
        barrier.reset();
    }
}
