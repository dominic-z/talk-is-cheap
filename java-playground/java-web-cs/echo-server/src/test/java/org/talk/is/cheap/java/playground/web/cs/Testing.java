package org.talk.is.cheap.java.playground.web.cs;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Testing
 * @date 2022/4/8 3:30 下午
 */
@Slf4j
public class Testing {

    @Test
    public void mt() throws InterruptedException {


        final ByteBuffer allocate = ByteBuffer.allocate(20);
        allocate.put("123".getBytes(StandardCharsets.UTF_8));

        allocate.flip();
        final byte b = allocate.get();
        allocate.compact();
        allocate.flip();

        final Object lock = new Object();

        final Thread thread1 = new Thread(() -> {
            synchronized (lock) {

                System.out.println("111");
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("111");
            }

        });
        thread1.start();
        Thread.sleep(20);


        final Thread thread2 = new Thread(() -> {
            synchronized (lock) {

                System.out.println("222");
                lock.notifyAll();
                System.out.println("222");
            }

        });
        thread2.start();

        thread2.join();

    }




}
