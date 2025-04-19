package concurrent.volatille;

//import sun.misc.Unsafe;

//import jdk.internal.misc.Unsafe;

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
}
