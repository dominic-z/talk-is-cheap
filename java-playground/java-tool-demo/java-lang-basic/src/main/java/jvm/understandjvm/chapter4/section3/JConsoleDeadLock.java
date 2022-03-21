package jvm.understandjvm.chapter4.section3;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author dominiczhu
 * @version 1.0
 * @title JConsoleDeadLock
 * @date 2021/10/18 下午4:38
 */
public class JConsoleDeadLock {

    private static Lock lock1 = new ReentrantLock();
    private static Lock lock2 = new ReentrantLock();

    /**
     * 线程死锁等待演示
     */
    static class SynAddRunalbe implements Runnable {
        Lock a, b;

        public SynAddRunalbe(Lock a, Lock b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public void run() {
            a.lock();

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("waiting for b");
            b.lock();
            System.out.println("get b");


            b.unlock();
            a.unlock();
        }
    }

    public static void main(String[] args) {
        System.out.println(lock1);
        System.out.println(lock2);
            new Thread(new SynAddRunalbe(lock1, lock2)).start();
            new Thread(new SynAddRunalbe(lock2, lock1)).start();
    }

}
