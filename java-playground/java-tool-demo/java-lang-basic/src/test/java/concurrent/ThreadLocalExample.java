package concurrent;

import org.junit.Test;

public class ThreadLocalExample {

    @Test
    public void inheritThreadLocalExample(){
        final InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();
        
        // 在主线程中设置 InheritableThreadLocal 的值
        inheritableThreadLocal.set("Value from parent thread");

        // 打印主线程中的值
        System.out.println("Main thread value: " + inheritableThreadLocal.get());

        // 创建一个子线程
        Thread childThread = new Thread(() -> {
            // 打印子线程中 InheritableThreadLocal 的值
            System.out.println("Child thread value: " + inheritableThreadLocal.get());

            // 在子线程中修改 InheritableThreadLocal 的值
            inheritableThreadLocal.set("Value from child thread");

            // 再次打印子线程中 InheritableThreadLocal 的值
            System.out.println("Modified child thread value: " + inheritableThreadLocal.get());
        });

        // 启动子线程
        childThread.start();

        try {
            // 等待子线程执行完毕
            childThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 再次打印主线程中的值，验证主线程不受子线程修改的影响
        System.out.println("Main thread value after child thread execution: " + inheritableThreadLocal.get());
    }
}
