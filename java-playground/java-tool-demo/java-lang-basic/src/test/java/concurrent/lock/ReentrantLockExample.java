package concurrent.lock;

import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockExample {

    @Test
    public void testLockInterruptibly() throws InterruptedException {

        ReentrantLock lock = new ReentrantLock();

        lock.lock();

        Thread thread = new Thread(() -> {
            System.out.printf("%s try get lock\n", Thread.currentThread());
            try {
//                lock.lock(); // 不会收到线程中断的影响
                lock.lockInterruptibly();
                System.out.printf("%s get lock\n", Thread.currentThread());
                lock.unlock();
            } catch (Exception e) {
                System.out.printf("%s is interrupted\n",Thread.currentThread());
                throw new RuntimeException(e);
            }

        });

        thread.start();
        Thread.sleep(10);
        System.out.println("thread.isInterrupted(): "+thread.isInterrupted());
        thread.interrupt();
//        thread.join();
        System.out.println("thread.isInterrupted(): "+thread.isInterrupted());



    }
}
