package section2.chapter19;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ExeutorPoolDemo
 * @date 2021/7/9 下午4:28
 */
public class ExeutorPoolDemo {


    // 创建2个线程的线程池


    public static void main(String[] args){
        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        Future<Object> abc = executor.submit(() -> {
            throw new RuntimeException("abc");
        });

        try {
            abc.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        executor.shutdown();

//        executor.awaitTermination();
    }
}
