package concurrent.jstack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dominiczhu
 * @version 1.0
 * @title WhileTrue
 * @date 2021/8/9 下午8:45
 */
public class WhileTrueTask implements Runnable {
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    public static Object lock = new Object();
    public void run() {
        synchronized (lock) {
            long sum = 0L;
            while (true) {
//                try {
                    sum += 1;

//                    Thread.sleep(300);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                //Do Nothing
            }
        }
    }

    public static void main(String[] args) {
        WhileTrueTask task1 = new WhileTrueTask();
        WhileTrueTask task2 = new WhileTrueTask();
        executorService.execute(task1);
        executorService.execute(task2);

    }
}
