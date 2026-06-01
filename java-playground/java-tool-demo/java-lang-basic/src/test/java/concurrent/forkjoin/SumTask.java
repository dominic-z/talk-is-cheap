package concurrent.forkjoin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * https://www.jdon.com/performance/threadpool-forkjoin.html
 *
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

    SumTask(long[] array, int start, int end, ForkJoinPool fjp, Thread parentThread) {
        this.array = array;
        this.start = start;
        this.end = end;
        this.fjp = fjp;
        this.parentThread = parentThread;
    }

    private String date2Str(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(d);
    }

    @Override
    protected Long compute() {

//        System.out.println(String.format("starting====start:%4d end: %4d time: %s, thread: %s",start,end,date2Str(new Date()),Thread
//        .currentThread()));
        System.out.println(String.format("starting====start:%4d end: %4d, thread: %s,parentThread State: %s, time: %s, " +
                "getActiveThreadCount: %d, getRunningThreadCount: %d", start, end, Thread.currentThread(), this.parentThread.getState(),
                date2Str(new Date()), fjp.getActiveThreadCount(), fjp.getRunningThreadCount()));

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
//            System.out.println(String.format("end     ====start:%4d end: %4d time: %s, getActiveThreadCount: %d, getRunningThreadCount:
//            %d",start,end,date2Str(new Date()),fjp.getActiveThreadCount(),fjp.getRunningThreadCount()));

            return sum;
        }
        // 任务太大,一分为二:
        int middle = (end + start) / 2;
//        System.out.println(String.format("split %d~%d ==> %d~%d, %d~%d", start, end, start, middle, middle, end));
        SumTask subtask1 = new SumTask(this.array, start, middle, fjp, Thread.currentThread());
        SumTask subtask2 = new SumTask(this.array, middle, end, fjp, Thread.currentThread());
        subtask1.fork();
        subtask2.fork(); // 直接在现在的线程执行任务，避免一次线程切换
//        invokeAll(subtask1, subtask2); // 等同于

        // 也可以手动提交
        // 向fjp提交一个任务，在fjp中每个worker有一个自己的任务队列，提交的时候可以向自己的任务队列里提交，而自己的任务队列里的任务可以被其他worker偷走
//        ForkJoinTask<Long> subtask1 = fjp.submit(new SumTask(this.array, start, middle,fjp,Thread.currentThread()));
//        ForkJoinTask<Long> subtask2 = fjp.submit(new SumTask(this.array, middle,end,fjp,Thread.currentThread()));


        //  这是我一直疑惑的地方，
        //  join操作会让当前线程等待subtask1的完成，那么按理来说当前线程会让当前线程交出cpu使用权，
        //  同理subtask1本身也会因为join被挂起，但问题是线程是有限的，这么递归下去如果线程池耗尽了所有的线程都挂起了，没有线程可用了
        //  后面的子任务就没法执行了呀
        /**
         * 不会傻等：如果该子任务还没执行完，线程 A 不会进入传统的阻塞休眠状态。
         * 工作窃取（Work Stealing）：线程 A 会利用这段等待时间，主动去窃取并执行其他任务队列中的任务（例如其他线程 fork 出来的子任务）。
         *
         * 证明：
         * starting====start:   0 end:  250, thread: Thread[ForkJoinPool-1-worker-1,5,main]
         * starting====start:   0 end:  125, thread: Thread[ForkJoinPool-1-worker-1,5,main]
         * 可以看到这个线程又去干别的活了start:   0 end:  125
         * https://www.qianwen.com/share/chat/b8b79316cf3e404881f34c114ee7bcd4
         *
         * 不同于普通的Thread.join()
         */
        Long subresult1 = subtask1.join();
        System.out.println(String.format("joining1====start:%4d end: %4d time: %s, Thread: %s, getActiveThreadCount: %d, " +
                "getRunningThreadCount: %d", start, end, date2Str(new Date()), Thread.currentThread(), fjp.getActiveThreadCount(),
                fjp.getRunningThreadCount()));
        Long subresult2 = subtask2.join();
        System.out.println(String.format("joining2====start:%4d end: %4d time: %s, Thread: %s getActiveThreadCount: %d, " +
                "getRunningThreadCount: %d", start, end, date2Str(new Date()), Thread.currentThread(), fjp.getActiveThreadCount(),
                fjp.getRunningThreadCount()));
        Long result = subresult1 + subresult2;
//        System.out.println("result = " + subresult1 + " + " + subresult2 + " ==> " + result);
        return result;
    }

    public static void main(String[] args) throws Exception {
        int a = 10;
        int b = (a = 10);
        System.out.println(b);

        // 创建随机数组成的数组:
        long[] array = new long[4000];
        fillRandom(array);

        // fork/join task:
        ForkJoinPool fjp = new ForkJoinPool(2); // 最大并发数4
        ForkJoinTask<Long> task = new SumTask(array, 0, array.length, fjp, Thread.currentThread());
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