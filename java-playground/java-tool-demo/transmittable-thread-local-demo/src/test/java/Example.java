import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Example {



    @Test
    public void testInheritableThreadLocal() throws InterruptedException {
        ThreadLocal<String> tl = new ThreadLocal<>();
        tl.set("hello ThreadLocal in Main");

//        // 在主线程中设置InheritableThreadLocal的值
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        itl.set("hello InheritableThreadLocal in Main");

        // 创建子线程的瞬间，子线程会copy主线程的inheritableThreadLocals
        Thread subThread = new Thread(()->{
            System.out.println("ThreadLocal value in new thread: " + tl.get());
            System.out.println("InheritableThreadLocal value in new thread: " + itl.get());
            itl.set("hello InheritableThreadLocal in subThread");
        });
        subThread.start();
        subThread.join();

        System.out.println(tl.get());
        System.out.println(itl.get()); // 子线程设置InheritableThreadLocal不会影响主线程

    }


    @Test
    public void testInheritableThreadLocalWontHelpInThreadPool() throws InterruptedException {

        // 前面的例子是主线程设置InheritableThreadLocal，然后创建子线程，这时子线程可以copy到主线程的InheritableThreadLocal
        // 但是在线程池场景，线程是先创建的，后面在程序运行过程中，某个线程A即使设置了InheritableThreadLocal并提交任务到这个线程池，
        // 线程池里的线程也无法获取这个线程A的InheritableThreadLocal，因为线程是先创建的
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        // 先提交一个任务，把这个线程创建起来，newFixedThreadPool并不会真正创建线程看源码
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // 在主线程中设置InheritableThreadLocal的值
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        itl.set("hello InheritableThreadLocal in Main");

        // 在线程池中执行任务
        executorService.execute(TtlRunnable.get(() -> {
            System.out.println("InheritableThreadLocal value in new thread: " + itl.get()); // 结果是null，因为这个子线程是先创建的
            itl.set("hello InheritableThreadLocal in subThread");
        }));

        // 等待任务执行完成
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        System.out.println(itl.get());

    }



    @Test
    public void testTransmittableThreadLocal() throws InterruptedException {

        // ttl解决了线程池的问题

        ExecutorService executorService = Executors.newFixedThreadPool(1); // 设置为一个线程，
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        // 在主线程中设置TransmittableThreadLocal的值
        TransmittableThreadLocal<String> ttl = new TransmittableThreadLocal<>();
        ttl.set("Hello, TransmittableThreadLocal in Main World!");
        // 在主线程中设置InheritableThreadLocal的值
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        itl.set("hello InheritableThreadLocal in Main");

        // 在线程池中执行任务
        executorService.execute(TtlRunnable.get(() -> {
            String value = ttl.get();
            System.out.println("TransmittableThreadLocal value in new thread: " + value);
            System.out.println("InheritableThreadLocal value in new thread: " + itl.get());
            ttl.set("Hello, subThread World!");
            itl.set("hello InheritableThreadLocal in subThread");
        }));

        // 等待任务执行完成
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        System.out.println(ttl.get());

    }
}
