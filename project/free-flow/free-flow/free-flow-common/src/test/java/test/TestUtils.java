package test;

import com.google.common.hash.Hashing;
import org.junit.jupiter.api.Test;
import org.talk.is.cheap.project.free.flow.common.exception.IllegalTaskDefinitionException;
import org.talk.is.cheap.project.free.flow.common.task.definition.codec.JsonInputCodec;
import org.talk.is.cheap.project.free.flow.common.task.definition.codec.SimpleStringInputCodec;
import org.talk.is.cheap.project.free.flow.common.utils.ReflectUtil;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestUtils {

    private final ThreadPoolExecutor taskDefinitionThreadPool = new ThreadPoolExecutor(0,1,1000, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

    @Test
    public void testHash(){
        System.out.println("192.168.78.7:8080".hashCode());
        System.out.println("192.168.78.7:8081".hashCode());

        // guava里封装了更好的hash算法
        System.out.println(Hashing.sha384().hashString("192.168.78.7:8081", StandardCharsets.UTF_8).asInt());
        System.out.println(Hashing.sha384().hashString("192.168.78.7:8080", StandardCharsets.UTF_8).asInt());
        System.out.println(Hashing.consistentHash(Hashing.sha384().hashString("192.168.78.7:8081", StandardCharsets.UTF_8).asInt(),4));
        System.out.println(Hashing.consistentHash(Hashing.sha384().hashString("192.168.78.7:8080", StandardCharsets.UTF_8).asInt(),4));
    }

    private void async(int id)  {
        try{
            Thread.sleep(1000);
            System.out.println(id);
            Thread.sleep(1000);
            System.out.println(id);
            Thread.sleep(1000);
            System.out.println(id);
            Thread.sleep(1000);
            System.out.println(id);
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testAsync() throws ExecutionException, InterruptedException {

        CompletableFuture.runAsync(()->this.async(1),taskDefinitionThreadPool);
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> this.async(2), taskDefinitionThreadPool);
        voidCompletableFuture.get();

    }


    class Param{
        int i;
    }
    class SomeCodec extends JsonInputCodec<Param> {

    }

    class SomeCodec2 extends SomeCodec{

    }
    @Test
    public void testReflectUtil() throws IllegalTaskDefinitionException {
        Class<?> codecGenericClass = ReflectUtil.getCodecGenericClass(SimpleStringInputCodec.class);
        System.out.println(codecGenericClass);
    }
}
