package concurrent.threadpool;

import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExample {

    /*
    abort会直接抛出异常
     */
    @Test
    public void abortHandler() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 4, 100, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<>(4),
                new ThreadPoolExecutor.AbortPolicy());
        for(int i=0;i<20;i++){
            try{

                threadPoolExecutor.execute(()->{
                    int k=0;
                    while(k<1000){
                        k++;
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }catch (Exception e){
                System.out.println(i+" abort exception");
                e.printStackTrace();
            }
        }

        System.out.println(threadPoolExecutor.isTerminated());
        System.out.println(threadPoolExecutor.isShutdown());
        threadPoolExecutor.awaitTermination(10000,TimeUnit.MICROSECONDS);
    }
}
