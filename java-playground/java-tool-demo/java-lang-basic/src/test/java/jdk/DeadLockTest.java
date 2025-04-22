package jdk;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 可以使用jdk中的jconsole来查看jvm情况，以及检测死锁，也可以使用jstack堆栈跟踪信息
 *  1, 使用jps查看java进程pid
 *  2. 使用jstack pid查看死锁情况
 *
 * @author dominiczhu
 * @version 1.0
 * @title DeadLockTest
 * @date 2021/8/9 下午8:48
 */
public class DeadLockTest {

    private static Lock lock1 = new ReentrantLock();
    private static Lock lock2 = new ReentrantLock();

    public static void deathLock() {
        Thread t1 = new Thread() {
            @Override
            public void run() {
                try {
                    lock1.lock();
                    System.out.println(Thread.currentThread().getName() + " get the lock1");
                    Thread.sleep(1000);
                    lock2.lock();
                    System.out.println(Thread.currentThread().getName() + " get the lock2");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t2 = new Thread() {
            @Override
            public void run() {
                try {
                    lock2.lock();
                    System.out.println(Thread.currentThread().getName() + " get the lock2");
                    Thread.sleep(1000);
                    lock1.lock();
                    System.out.println(Thread.currentThread().getName() + " get the lock1");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        //设置线程名字，方便分析堆栈信息
        t1.setName("mythread-jay");
        t2.setName("mythread-tianluo");
        t1.start();
        t2.start();
    }
    public static void main(String[] args) {
        deathLock();
    }

}
