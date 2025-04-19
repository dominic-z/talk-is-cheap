package concurrent.volatille;

//import sun.misc.Unsafe;

//import jdk.internal.misc.Unsafe;

import org.junit.Test;

import java.lang.reflect.Field;

public class VolatileDemo {

    static class ChangeThread implements Runnable{
//        volatile boolean flag=false;

        // 不加volatile的话会一直循环
        boolean flag=false;
        @Override
        public void run() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("subThread change flag to:" + flag);
            flag = true;
        }

        public boolean isFlag() {
            return flag;
        }
    }

//    private static Unsafe reflectGetUnsafe() {
//        try {
//            Field field = Unsafe.class.getDeclaredField("theUnsafe");
//            field.setAccessible(true);
//            return (Unsafe) field.get(null);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
    public static void main(String[] args){
        ChangeThread changeThread = new ChangeThread();
//        Unsafe unsafe = reflectGetUnsafe();
        new Thread(changeThread).start();
        while (true) {
            boolean flag = changeThread.isFlag();
//            unsafe.loadFence(); //加入读内存屏障
            if (flag){
                System.out.println("detected flag changed");
                break;
            }
        }
        System.out.println("main thread end");
    }


    private static int a = 0;
    private static volatile boolean flag = false;

    public static void writer() {
        a = 1;         // 语句 1
        flag = true;   // 语句 2
    }

    public static void reader() {
        if (flag) {    // 语句 3
            int i = a; // 语句 4
            System.out.println(i);
        }
    }

    @Test
    public void reorder(){
        // 在这个例子中，由于 flag 被 volatile 修饰，语句 1 一定会在语句 2 之前执行，语句 3 一定会在语句 4 之前执行，避免了指令重排序带来的问题。
        writer();
        reader();
    }
}
