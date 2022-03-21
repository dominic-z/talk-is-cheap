package section2.chapter29;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author dominiczhu
 * @version 1.0
 * @title UseCompleteFuture
 * @date 2021/7/20 上午9:50
 */
public class UseCompleteFuture {

    @Test
    public void thenXXX() throws ExecutionException, InterruptedException {

        CompletableFuture<String> f0 =
                CompletableFuture.supplyAsync(
                        () -> {
                            System.out.println("first" + Thread.currentThread());
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return "Hello World";
                        })      //①
                        .thenApplyAsync(s -> {
                            System.out.println("second " + Thread.currentThread());
                            return s + " QQ";
                        })  //②
                        .thenApply(s -> {
                            System.out.println("final " + Thread.currentThread());
                            return s.toUpperCase();
                        });//③
        System.out.println("out " + Thread.currentThread());
        System.out.println(f0.join());


    }

}
