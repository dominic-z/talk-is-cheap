package section1.chapter1;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Test
 * @date 2021/6/21 上午9:38
 */

public class Test {
    private long count = 0;
    private void add10K() {
        int idx = 0;
        while(idx++ < 10000) {
            count += 1;
        }
    }
    public static long calc() throws InterruptedException {
        final Test test = new Test();
        // 创建两个线程，执行add()操作
        Thread th1 = new Thread(()->{
            test.add10K();
        });
        Thread th2 = new Thread(()->{
            test.add10K();
        });
        // 启动两个线程
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        th1.join();
        th2.join();
        return test.count;
    }

    public static void main(String[] args) throws InterruptedException {
        long count = calc();

        System.out.println(count);
    }
}
