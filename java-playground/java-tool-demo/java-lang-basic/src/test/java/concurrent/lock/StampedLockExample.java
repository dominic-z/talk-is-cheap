package concurrent.lock;

import jakarta.xml.ws.Holder;
import org.junit.Test;

import java.util.concurrent.locks.StampedLock;

public class StampedLockExample {

    private static void log(String s) {
        System.out.printf("[%s] %s \n", Thread.currentThread(), s);
    }

    /**
     * 输出
     * [Thread[main,5,main]] get readlock
     * [Thread[Thread-0,5,main]] success to get OptimisticRead lock and resource is not changed: 1
     * [Thread[main,5,main]] get writeLock
     * [Thread[Thread-0,5,main]] success to get read lock but resource is changed: 1
     * [Thread[Thread-0,5,main]] success to get OptimisticRead lock and resource is not changed: 2
     * @throws InterruptedException
     */
    @Test
    public void optimisticRead() throws InterruptedException {
        final StampedLock sl = new StampedLock();
        Holder<Integer> resource = new Holder<>(1);

        Thread readThread = new Thread(() -> {
            while (true) {
                long stamp = sl.tryOptimisticRead();
                int v = resource.value;
                if (stamp != 0) {
                    // 说明成功获取了乐观读锁
                    // 等待一段事件，用来模拟
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if (sl.validate(stamp)) {
                        log("success to get OptimisticRead lock and resource is not changed: " + v);
                    } else {
                        // 说明成功获得了乐观读锁，但是之前的乐观读的resource被修改了，需要升级为读锁
                        long rStamp = sl.readLock();
                        log("success to get read lock but resource is changed: " + v);
                        sl.unlockRead(rStamp);
                    }
                } else {
                    log("fail to get OptimisticRead sleep ");
                }

            }
        });

        // 读锁和乐观读不互斥
        long stamp = sl.tryReadLock();
        if (stamp != 0) {
            try {
                log("get readlock");
                readThread.start();
                // 睡眠一下，让eadThread去执行乐观读
                Thread.sleep(1500);
            } finally {
                sl.unlockRead(stamp);
            }
        }

        // 到这个时候，readThread刚好应该在sleep里，差不多还有400ms唤醒。
        long writeLockStamp = sl.writeLock();
        try {
            if (writeLockStamp != 0) {
                log("get writeLock");
                resource.value = 2;
            }
        } finally {
            sl.unlockWrite(writeLockStamp);
        }
        readThread.join();
    }

}
