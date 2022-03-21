package jvm.understandjvm.chapter7.section3;

/**
 * @author dominiczhu
 * @version 1.0
 * @title DeadLoopClass
 * @date 2021/10/20 下午1:58
 */
public class DeadLoopClass {

    private static class Inner {
        static {
// 如果不加上这个if语句，编译器将提示“Initializer does not complete normally"并拒绝编译
            if (true) {
                System.out.println(Thread.currentThread() + "init DeadLoopClass");
                while (true) {
                }
            }
        }
    }

    public static void main(String[] args) {
        Runnable script = new Runnable() {
            public void run() {
                System.out.println(Thread.currentThread() + "start");
                Inner dlc = new Inner();
                System.out.println(Thread.currentThread() + " run over");
            }
        };
        Thread thread1 = new Thread(script);
        Thread thread2 = new Thread(script);
        thread1.start();
        thread2.start();
    }
}
