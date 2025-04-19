package jdk.jstack;

/**
 * @author dominiczhu
 * @version 1.0
 * @title InvokeWait
 * @date 2021/8/11 上午9:55
 */
public class InvokeWait {
    public static void main(String[] args){
        Object scarce = new Object();
        System.out.println(scarce.hashCode());
        System.out.println(scarce);
        Thread t = new Thread(new SyncWaiter(scarce));
        t.setName("Test-Thread-1");
        t.start();
    }
}

class SyncWaiter implements Runnable {
    private Object scarce;
    public SyncWaiter(Object scarce){
        this.scarce = scarce;
    }

    @Override
    public void run() {
        synchronized (scarce) {
            try {
                scarce.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}