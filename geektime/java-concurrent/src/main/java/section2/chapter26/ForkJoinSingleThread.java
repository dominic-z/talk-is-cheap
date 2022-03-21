package section2.chapter26;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * @author dominiczhu
 * @version 1.0
 * @title SumTask
 * @date 2021/7/22 上午9:54
 */
public class ForkJoinSingleThread extends RecursiveTask<Long> {

    long i;

    ForkJoinSingleThread(long i) {
        this.i = i;
    }

    @Override
    protected Long compute() {

        if (i <= 0) {
            // 如果任务足够小,直接计算:
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            return i;
        }
        // 任务太大,一分为二:
        ForkJoinSingleThread subtask1 = new ForkJoinSingleThread(i-1);
        subtask1.fork();

        Long subresult1 = subtask1.join();
        return subresult1;
    }

    public static void main(String[] args) {
        // 创建随机数组成的数组:
        ForkJoinPool fjp = new ForkJoinPool(1); // 最大并发数1
        ForkJoinTask<Long> task = new ForkJoinSingleThread(10);
        long startTime = System.currentTimeMillis();
        Long result = fjp.invoke(task);
        long endTime = System.currentTimeMillis();
        System.out.println("Fork/join sum: " + result + " in " + (endTime - startTime) + " ms.");
    }
}