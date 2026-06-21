package concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LockDemo {

    private final Map<String, Object> lockMap = new HashMap<>();

    private int i = 0;

    private Object getLock(String name){
        if(lockMap.containsKey(name)){
            return lockMap.get(name);
        }

        synchronized (this){
            if(lockMap.containsKey(name)){
                return lockMap.get(name);
            }
            lockMap.put(name,new Object());
        }
        return lockMap.get(name);
    }

    private void add(String lockName){
        Object lock = getLock(lockName);
//        synchronized (lock){
            this.i +=1;
//        }
    }

    public static void main(String[] args) throws InterruptedException {
        LockDemo lockDemo = new LockDemo();

        String name = "n";
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {

            Thread thread = new Thread(() -> {
                for (int j = 0; j < 100000; j++) {
                    lockDemo.add(name);
                }
            });
            threads.add(thread);
            thread.start();
        }
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(lockDemo.i);
    }


}
