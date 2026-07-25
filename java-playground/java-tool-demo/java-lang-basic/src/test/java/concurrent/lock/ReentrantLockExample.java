package concurrent.lock;

import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockExample {


    @Test
    public void testLock() throws InterruptedException {

        ReentrantLock lock = new ReentrantLock();

        Thread t1 = new Thread(() -> {
            lock.lock();
            while(true){

            }
        },"可重入锁1");
        t1.start();

        Thread.sleep(20);
        Thread t2 = new Thread(() -> {
            lock.lock();
        },"可重入锁2");
        // 注意，t2线程的状态是java.lang.Thread.State: WAITING (parking)

        t2.start();
        t2.join();
    }

    @Test
    public void testLockInterruptibly() throws InterruptedException {

        ReentrantLock lock = new ReentrantLock();

        lock.lock();

        Thread thread = new Thread(() -> {
            System.out.printf("%s try get lock\n", Thread.currentThread());
            try {
                lock.lock(); // 不会收到线程中断的影响
//                lock.lockInterruptibly();
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
