package io;

import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;


public class ReaderWriterWatcher {

    /*
    PrintWriter 是 Java 中一个非常实用的类，它位于 java.io 包下，用于将格式化的对象和基本数据类型输出到文本输出流。
    关键在于格式化
     */
    @Test
    public void printWriterExample() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("git_ignore/output.txt"), true)) {
            // 输出字符串
            writer.println("Hello, World!");
            // 输出整数
            writer.println(123);
            // 格式化输出
            writer.printf("The value of pi is approximately %.2f%n", Math.PI);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void makeIgnoreDir() {
        Path gitIgnore = Paths.get("git_ignore");
        if(!Files.exists(gitIgnore) ||  !Files.isDirectory(gitIgnore)){
            try {
                Files.createDirectory(gitIgnore);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }



    @Test
    public void watchFile(){
        try {
            // 创建 WatchService 实例
            WatchService watchService = FileSystems.getDefault().newWatchService();
            // 要监控的目录
            Path dir = Paths.get("git_ignore");
            // 注册监控事件，这里监控文件的创建、修改和删除事件
            // 只能监控目录
            dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            System.out.println("开始监控目录: " + dir);

            while (true) {
                // 获取下一个监控事件
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    // 获取事件类型
                    WatchEvent.Kind<?> kind = event.kind();
                    // 获取发生变化的文件或目录的名称
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        // 事件溢出，忽略
                        System.err.println("事件溢出，有部分事件丢失了");
                        continue;
                    }

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        System.out.println("文件/目录创建: " + fileName);
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        System.out.println("文件/目录修改: " + fileName);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        System.out.println("文件/目录删除: " + fileName);
                    }
                }

                // 重置 WatchKey，以便继续接收事件
                boolean valid = key.reset();
                if (!valid) {
                    // 若 WatchKey 无效，退出循环
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
