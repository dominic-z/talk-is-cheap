package concurrent.future;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureExample {

    @Test
    public void testThenCompose() throws ExecutionException, InterruptedException {
        CompletableFuture<String> taskA = CompletableFuture.supplyAsync(() -> "taskA");

        // 两个future需要顺序执行的时候。这种情况需要用thenCompose
        CompletableFuture<String> compose = taskA.thenCompose((resA) -> CompletableFuture.supplyAsync(() -> resA+11));
//        CompletableFuture<CompletableFuture<String>> thenA =
//                taskA.thenApply((resA) -> CompletableFuture.supplyAsync(() -> resA + 11));

        System.out.println(compose.get());
//        System.out.println(thenA.get());


    }
}
