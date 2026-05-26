package concurrent.volatille;

//import sun.misc.Unsafe;

//import jdk.internal.misc.Unsafe;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

public class VolatileDemo {

    static class ChangeThread implements Runnable {
        // 不加volatile的话会一直循环
        volatile boolean flag = false;
        int[] padding = new int[10240];
        int a = 0;

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("subThread change flag to:" + flag);
            a = 10;
            Arrays.fill(padding, 1);
            flag = true;
            while (true){

            }
        }

        public boolean isFlag() {
            return flag;
        }
    }

    @Test
    public void testVolatileWrite() {
        ChangeThread changeThread = new ChangeThread();
        int x = changeThread.a;
        new Thread(changeThread).start();
        while (true) {
            boolean flag = changeThread.isFlag();
//            unsafe.loadFence(); //加入读内存屏障
            if (flag) {
                System.out.println("detected flag changed");
                System.out.println("a is " + changeThread.a);
                break;
            }
        }
        System.out.println("main thread end");
    }

}
