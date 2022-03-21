package jvm.understandjvm.chapter4;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author dominiczhu
 * @version 1.0
 * @title JStackAndLock
 * @date 2021/10/17 下午10:12
 */
public class JStackAndLock {


    private static Object lockKey1 = new Object();
    private static Object lockKey2 = new Object();
    private static Lock lock1 = new ReentrantLock();
    private static Lock lock2 = new ReentrantLock();

    public static void lockDeadLock() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                lock1.lock();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("thread1 waiting lockKey2");
                lock2.lock();

                System.out.println("get LockKey2");
                lock2.unlock();
                lock1.unlock();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                lock2.lock();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("thread2 waiting lockKey1");
                lock1.lock();

                System.out.println("get LockKey2");
                lock1.unlock();
                lock2.unlock();
            }
        }).start();
    }

    public static void synchronizedDeadLock() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lockKey1) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread1 waiting lockKey2");

                    synchronized (lockKey2) {
                        System.out.println("get LockKey2");
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lockKey2) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread2 waiting lockKey1");

                    synchronized (lockKey1) {
                        System.out.println("get LockKey1");
                    }
                }
            }
        }).start();

    }

    public static void main(String[] args) throws InterruptedException
    {

        // 内置锁和lock的jstack差不太多
//        synchronizedDeadLock();
        lockDeadLock();
    }
}
