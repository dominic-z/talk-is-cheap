package concurrent.forkjoin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * https://www.jdon.com/performance/threadpool-forkjoin.html
 * @author dominiczhu
 * @version 1.0
 * @title SumTask
 * @date 2021/7/22 上午9:54
 */
public class SumTask extends RecursiveTask<Long> {

    static final int THRESHOLD = 100;
    long[] array;
    int start;
    int end;
    ForkJoinPool fjp;

    Thread parentThread;

    SumTask(long[] array, int start, int end,ForkJoinPool fjp,Thread parentThread) {
        this.array = array;
        this.start = start;
        this.end = end;
        this.fjp=fjp;
        this.parentThread = parentThread;
    }

    private String date2Str(Date d){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(d);
    }

    @Override
    protected Long compute() {

//        System.out.println(String.format("starting====start:%4d end: %4d time: %s, thread: %s",start,end,date2Str(new Date()),Thread.currentThread()));
        System.out.println(String.format("starting====start:%4d end: %4d, thread: %s,parentThread State: %s, time: %s, getActiveThreadCount: %d, getRunningThreadCount: %d",start,end,Thread.currentThread(),this.parentThread.getState(),date2Str(new Date()),fjp.getActiveThreadCount(),fjp.getRunningThreadCount()));

        if (end - start <= THRESHOLD) {
            // 如果任务足够小,直接计算:
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
            }
//            System.out.println(String.format("compute %d~%d = %d", start, end, sum));
//            System.out.println(String.format("end     ====start:%4d end: %4d time: %s, getActiveThreadCount: %d, getRunningThreadCount: %d",start,end,date2Str(new Date()),fjp.getActiveThreadCount(),fjp.getRunningThreadCount()));

            return sum;
        }
        // 任务太大,一分为二:
        int middle = (end + start) / 2;
//        System.out.println(String.format("split %d~%d ==> %d~%d, %d~%d", start, end, start, middle, middle, end));
//        SumTask subtask1 = new SumTask(this.array, start, middle,fjp);
//        SumTask subtask2 = new SumTask(this.array, middle, end,fjp);
//        invokeAll(subtask1, subtask2);
        // 向fjp提交一个任务，在fjp中每个worker有一个自己的任务队列，提交的时候可以向自己的任务队列里提交，而自己的任务队列里的任务可以被其他worker偷走
        ForkJoinTask<Long> subtask1 = fjp.submit(new SumTask(this.array, start, middle,fjp,Thread.currentThread()));
        ForkJoinTask<Long> subtask2 = fjp.submit(new SumTask(this.array, middle,end,fjp,Thread.currentThread()));
//        subtask1.fork();
//        subtask2.fork();

        // todo: 这是我一直疑惑的地方，join操作我认为一定会让当前线程交出cpu使用权，或者说要将当前的任务修改为waiting状态然后去让当前线程去执行其他的额任务，只有这样才能让fork的各个子任务执行下去，否则当前任务一直把持着cpu不交出cpu执行权，后面的子任务就没法执行了呀
        // 就像Thread.join一样
        Long subresult1 = subtask1.join();
        System.out.println(String.format("joining1====start:%4d end: %4d time: %s, Thread: %s, getActiveThreadCount: %d, getRunningThreadCount: %d",start,end,Thread.currentThread(),date2Str(new Date()),fjp.getActiveThreadCount(),fjp.getRunningThreadCount()));
        Long subresult2 = subtask2.join();
        System.out.println(String.format("joining2====start:%4d end: %4d time: %s, Thread: %s getActiveThreadCount: %d, getRunningThreadCount: %d",start,end,Thread.currentThread(),date2Str(new Date()),fjp.getActiveThreadCount(),fjp.getRunningThreadCount()));
        Long result = subresult1 + subresult2;
//        System.out.println("result = " + subresult1 + " + " + subresult2 + " ==> " + result);
        return result;
    }

    public static void main(String[] args) throws Exception {
        int a=10;
        int b=(a=10);
        System.out.println(b);

        // 创建随机数组成的数组:
        long[] array = new long[400];
        fillRandom(array);

        // fork/join task:
        ForkJoinPool fjp = new ForkJoinPool(4); // 最大并发数4
        ForkJoinTask<Long> task = new SumTask(array, 0, array.length,fjp,Thread.currentThread());
        long startTime = System.currentTimeMillis();
        Long result = fjp.invoke(task);
        long endTime = System.currentTimeMillis();
        System.out.println("Fork/join sum: " + result + " in " + (endTime - startTime) + " ms.");
    }

    private static void fillRandom(long[] array) {
        Random random = new Random();

        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextLong();

        }
    }

}