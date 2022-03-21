package section1.chapter4;

/**
 * @author dominiczhu
 * @version 1.0
 * @title FailLock
 * @date 2021/6/26 上午10:37
 */

public class FailLock {


    public static void main(String[] args) {



        Thread th1=new Thread(()->{

        });
    }
}

class Task implements Runnable{

    final Object lock;
    public Task(Object obj) {
        this.lock=obj;
    }

    @Override
    public void run() {
        synchronized (lock){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}