import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Example {

    @Test
    public void test() throws InterruptedException {
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        itl.set("hello InheritableThreadLocal");

        TransmittableThreadLocal<String> ttl = new TransmittableThreadLocal<>();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // 在主线程中设置TransmittableThreadLocal的值
        ttl.set("Hello, World!");

        // 在线程池中执行任务
        executorService.execute(TtlRunnable.get(() -> {
            String value = ttl.get();
            System.out.println("TransmittableThreadLocal value in new thread: " + value);
            System.out.println("InheritableThreadLocal value in new thread: " + itl.get());
        }));

        // 等待任务执行完成
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

    }
}
