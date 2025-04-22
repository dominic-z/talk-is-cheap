package jdk;

import jvm.domain.Apple;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class GCDemo {

    static class Task implements Callable<String>{
        List<byte[]> list;
        int mb;
        Task(int mb){
            this.list = new ArrayList<>();
            this.mb = mb;
        }
        @Override
        public String call() throws Exception {
            for(int i=0;i<100;i++){
                // 每次创建一个 MB 的字节数组
                byte[] largeObject = new byte[1024 * 1024 * mb];
                if(i%2==0){
                    list.add(largeObject);
                }
                Thread.sleep(100);
            }
            return "done";
        }

    }
    /**
     * 使用jps查看java进程
     * 使用jstat -gc java进程id -1000  查看gc情况
     *
     */


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        LinkedList<byte[]> list = new LinkedList<>();
        Random random = new Random();

        Task task1 = new Task(1);
        Future<String> f1 = executorService.submit(task1);
        Task task2 = new Task(1);
        Future<String> f2 = executorService.submit(task2);

        f1.get();
        f2.get();

        // 释放内存
        task1 = null;
        task2=null;

        Task task3 = new Task(10);
        Future<String> f3 = executorService.submit(task3);

        f3.get();
        Thread.sleep(100000);
    }
}
