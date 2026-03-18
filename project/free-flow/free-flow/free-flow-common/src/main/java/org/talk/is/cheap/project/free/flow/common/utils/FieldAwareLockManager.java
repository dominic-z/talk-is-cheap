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

    private final Map<K, LockWrapper> lockMap = new ConcurrentHashMap<>();
    private final Map<K, AtomicInteger> referrencCountMap = new ConcurrentHashMap<>();

    // 锁包装器：包含锁实例 + 引用计数（用于安全清理）
    private static class LockWrapper {
        final ReentrantLock lock = new ReentrantLock();
        final AtomicInteger refCount = new AtomicInteger(1); // 创建即计数=1
    }

    /**
     * 加锁（可中断）
     *
     * @param key 锁标识（非空）
     * @throws InterruptedException     线程在等待锁时被中断
     * @throws IllegalArgumentException key 为空
     */
    public void lock(K key) throws InterruptedException {
        validateKey(key);
        LockWrapper wrapper = getOrCreateWrapper(key);
        try {
            wrapper.lock.lockInterruptibly(); // 支持响应中断
        } catch (InterruptedException e) {
            releaseWrapper(key, wrapper); // 加锁失败需释放引用
            throw e;
        }
    }

    /**
     * 尝试加锁（带超时）
     *
     * @return true=获取成功，false=超时
     */
    public boolean tryLock(K key, long timeout, TimeUnit unit) throws InterruptedException {
        validateKey(key);
        LockWrapper wrapper = getOrCreateWrapper(key);
        boolean acquired = false;
        try {
            acquired = wrapper.lock.tryLock(timeout, unit);
            if (!acquired) releaseWrapper(key, wrapper);
            return acquired;
        } catch (InterruptedException e) {
            releaseWrapper(key, wrapper);
            throw e;
        }
    }

    /**
     * 解锁
     *
     * @throws IllegalStateException    未持有锁 / key 未加锁 / 跨线程解锁
     * @throws IllegalArgumentException key 为空
     */
    public void unlock(K key) {
        validateKey(key);
        LockWrapper wrapper = lockMap.get(key);
        if (wrapper == null) {
            throw new IllegalStateException("非法操作：对未加锁的 key [" + key + "] 调用 unlock");
        }
        // 严格校验：必须由持有锁的线程调用
        if (!wrapper.lock.isHeldByCurrentThread()) {
            throw new IllegalStateException(
                    "安全拒绝：当前线程未持有 key [" + key + "] 的锁，禁止跨线程解锁");
        }
        try {
            wrapper.lock.unlock();
        } finally {
            releaseWrapper(key, wrapper); // 释放后自动清理
        }
    }

    /**
     * 尝试解锁
     * @param key
     * @return
     */
    public boolean tryUnlock(K key) {
        validateKey(key);
        if (!lockMap.containsKey(key) || !lockMap.get(key).lock.isLocked() || !lockMap.get(key).lock.isHeldByCurrentThread()) {
            return false;
        }
        unlock(key);
        return true;
    }

    // ========== 内部工具方法 ==========

    private void validateKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("锁 key 不能为空");
        }
    }

    // 原子获取或创建锁包装器（引用计数+1）
    private LockWrapper getOrCreateWrapper(K key) {
        return lockMap.compute(key, (k, existing) -> {
            if (existing != null) {
                existing.refCount.incrementAndGet(); // 复用时计数+1
                return existing;
            }
            return new LockWrapper(); // 新建时 refCount=1
        });
    }

    // 释放引用：解锁后调用，计数归零时安全移除
    private void releaseWrapper(K key, LockWrapper wrapper) {
        if (wrapper.refCount.decrementAndGet() == 0) {
            // CAS 移除：防止其他线程刚增加引用
            lockMap.computeIfPresent(key, (k, current) ->
                    current == wrapper && current.refCount.get() == 0 ? null : current
            );
        }
    }

    // ========== 监控方法（可选）==========

    /**
     * 获取当前活跃锁数量（用于监控）
     */
    public int getActiveLockCount() {
        return lockMap.size();
    }

    /**
     * 检查当前线程是否持有指定 key 的锁
     */
    public boolean isHeldByCurrentThread(K key) {
        LockWrapper wrapper = lockMap.get(key);
        return wrapper != null && wrapper.lock.isHeldByCurrentThread();
    }


    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 20, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        final FieldAwareLockManager<Integer> lockManager = new FieldAwareLockManager<>();
        Map<Integer, Integer> count = new ConcurrentHashMap<>();
        Random random = new Random();
        int max = 10000;
        int fieldNum = 1000;
        for (int i = 0; i < max; i++) {
            int finalI = i / fieldNum; // 加大竞争
            threadPoolExecutor.submit(() -> {

                try {

                    lockManager.lock(finalI);
                    lockManager.lock(finalI);
                    Thread.sleep(1);
                    count.put(finalI, count.computeIfAbsent(finalI, (k) -> 0) + 1);
                    lockManager.tryUnlock(finalI);
                    lockManager.tryUnlock(finalI);
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
                if (v != fieldNum) {
                    System.out.println(k + ":" + v);
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
