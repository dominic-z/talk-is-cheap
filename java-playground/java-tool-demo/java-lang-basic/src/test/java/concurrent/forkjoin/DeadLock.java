package concurrent.forkjoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class DeadLock extends RecursiveTask<Integer> {


    Integer i;

    DeadLock(Integer i) {
        this.i = i;
    }

    @Override
    protected Integer compute() {
        System.out.println(String.format("%d %s", i, Thread.currentThread()));
        if (i <= 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return i;
        }

        DeadLock sub1 = new DeadLock(i - 1);
        sub1.fork();
        int res= sub1.join();

        return res;
    }

    public static void main(String[] args) {

        DeadLock deadLock = new DeadLock(10);
        ForkJoinPool forkJoinPool = new ForkJoinPool(1);
        int i = forkJoinPool.invoke(deadLock);

        System.out.println(i);

    }
}
