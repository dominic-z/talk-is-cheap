package org.talk.is.cheap.project.free.flow.common.utils;


import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 参考https://www.doubao.com/thread/we6464b5ffbe59b87
 * 这个答案有问题，我进行了追问，其实豆包的补充回答也还是有问题的，referenceCountMap.remove(fieldValue, currentCount)即使为true，如果随后其他线程又执行了getLock操作，还是会有问题，
 * 借鉴个思路吧
 * <p>
 * <p>
 * 是一个锁管理器，这个锁管理器可以根据字段的不同进行加锁，k相同则将会获取相同的锁
 */
public class FieldAwareLockManager<K> {

    private final Map<K, Lock> lockMap = new ConcurrentHashMap<>();
    private final Map<K, AtomicInteger> referrencCountMap = new ConcurrentHashMap<>();


    /*
     * 版本1
     * 加锁：向map中创建锁对象->加锁
     * 解锁：释放锁->删除锁对象
     *
     * 但这有并发问题啊shit
     * 线程A:  释放锁                            删除锁对象
     * 线程B:             获取锁并尝试加锁
     * 最终结果就是这个锁不在map中了。
     *
     * 版本2：
     * 加锁：向map中创建锁对象->加锁->再次尝试放入map中
     * 解锁：释放锁->删除锁对象
     *
     * 还是有问题
     * 线程A:  释放锁                                                删除锁对象
     * 线程B:             获取锁并尝试加锁   次尝试放入map中
     * 最终结果就是这个锁不在map中了。
     *
     *
     * 版本3是下面的代码，我希望利用reentrantlock的可重入特性解锁，但还是有问题，并发测试仍然不通过，unlock方法会抛出空指针异常和IllegalMonitorStateException异常
     * 这说明unlock的时候，lockMap中某个key对应lock被删除或者替换过，是这样造成的，想了一上午，很难发现
     *
     * 线程A： 执行完lock(1)方法 --->                   --> 执行完lockMap.remove(1);
     * 线程B：                 --->  执行到lock.lock();这个lock对象与线程A的lock对象是同一个对象，记作lockA
     * 线程C :                 --->                    -->                          --> 执行到lock(1)的computeIfAbsent，这是一个全新的lock对象lockB
     *
     * 随后
     * 线程A：  释放lockA
     * 线程B：                                                 lockA加锁，但不会被put至lockMap中 -->  unlock(1)就报错了，因为此时1->lockB
     * 线程C : 成功完成lock(1)方法，此时lockMap中1->lockB   --->                                 -->
     *
     *
     * 线程C :        nihao nihao mouse mouse is coming hahahahahahah
     *
     */

//    public void lock(K key) {
//        // lockMap.computeIfAbsent(key, (n) -> new ReentrantLock())是原子操作，多个线程同时执行这个操作，返回的值一定是同一个value
//        Lock lock = lockMap.computeIfAbsent(key, (k) -> new ReentrantLock());
//        lock.lock();
//        // 这一步的作用是确保上锁的lock存在在map里
//
//        lockMap.putIfAbsent(key, lock);
//    }
//
//    public void unlockAndRemove(K k) {
//        Lock lock = lockMap.get(k);
//        lockMap.remove(k);
//        lock.unlock();
//    }


    /**
     * 终极解决方法，基于上述问题，借鉴单例模式的两次校验来优化
     *
     * @param key
     */
    public void lock(K key) {
        // lockMap.computeIfAbsent(key, (n) -> new ReentrantLock())是原子操作，多个线程同时执行这个操作，返回的值一定是同一个value
        Lock lock = lockMap.computeIfAbsent(key, (k) -> new ReentrantLock());
        lock.lock();
        // 加锁后二次校验，确保lock对象在map中，
        // 只有lock在map中，就能确保其余key相同的lock(k)方法中使用的锁是同一把锁，从而确保lock.lock能够被成功阻塞
        if (lock != lockMap.get(key)) {
            // 如果到这里，说明lockMap中的k对应的lock已经不是lock这个对象了，也就是说lock.lock的加锁是无效的，因此重新加锁
            lock.unlock();
            lock(key);
        }
    }

    public void unlockAndRemove(K k) {
        Lock lock = lockMap.remove(k);// 其实这一步就相当于解锁了
        if(lock!=null){
            lock.unlock();
        }
    }


    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 20, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        final FieldAwareLockManager<Integer> lockManager = new FieldAwareLockManager<>();
        Map<Integer, Integer> count = new ConcurrentHashMap<>();
        Random random = new Random();
        for (int i = 0; i < 10000000; i++) {

            int finalI = i / 100000; // 加大竞争
            threadPoolExecutor.submit(() -> {

                try {

                    lockManager.lock(finalI);
                    count.put(finalI, count.computeIfAbsent(finalI, (k) -> 0) + 1);
                    lockManager.unlockAndRemove(finalI);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }
        threadPoolExecutor.shutdown();
        try {
            while (!threadPoolExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {

            }
            count.forEach((k, v) -> {
                if (v != 100000) {
                    System.out.println(k);
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
