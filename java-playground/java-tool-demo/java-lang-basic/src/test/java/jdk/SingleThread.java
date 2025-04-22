package jdk;

/**
 * @author dominiczhu
 * @version 1.0
 * @title SingleThread
 * @date 2021/8/9 下午8:46
 */
public class SingleThread {

    private static class ThreadRunner implements Runnable{
        @Override
        public void run() {
            while(true){
//            System.out.println(1);
            }
        }
    }

    public void jstackThread() {
        Thread thread = new Thread(new ThreadRunner());
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SingleThread obj=new SingleThread();
        obj.jstackThread();
    }

}

