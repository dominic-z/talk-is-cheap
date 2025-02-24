package cn.itcast.netty.c3;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.NettyRuntime;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
//import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * 对应3.1
 * 由此课件EventLoop就是一个线程，在netty里可以用来处理io操作
 */
@Slf4j
public class TestEventLoop {
    @Test
    public void eventLoopDemo() throws InterruptedException {
        // 1. 创建事件循环组
        EventLoopGroup group = new NioEventLoopGroup(2); // io 事件，普通任务，定时任务
//        EventLoopGroup group = new DefaultEventLoopGroup(); // 普通任务，定时任务
        // 2. 获取下一个事件循环对象
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());

        // 3. 执行普通任务
        group.next().execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("current thread:{} ok", Thread.currentThread());
        });

        // 4. 执行定时任务
        group.next().scheduleAtFixedRate(() -> {
            log.debug("schedule task current thread:{} ok", Thread.currentThread());
        }, 0, 1, TimeUnit.SECONDS);
        Thread.sleep(3000);
        log.debug("main thread:{} ok", Thread.currentThread());

        // 让主进程能暂停，避免主线程结束，子线程也跟着结束
        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
