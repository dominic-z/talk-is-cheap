package io;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RandomAccess {


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

    /*
    randomAccessFile可以用于断点续传，比如上传了一个5g大文件，分片成10份，接收到第5份的一半断掉了，重新上传的时候可以从第5份的起始位置上传
     */
    @Test
    public void randomReadWrite(){
        try (RandomAccessFile raf = new RandomAccessFile("git_ignore/test.txt", "rw")) {
            // 写入数据
            raf.writeUTF("Hello, RandomAccessFile!");
            raf.writeInt(123);
            raf.writeDouble(3.14);

            // 将文件指针移动到文件开头
            raf.seek(0);

            // 读取数据
            String str = raf.readUTF();
            int num = raf.readInt();
            double d = raf.readDouble();

            System.out.println("读取的字符串: " + str);
            System.out.println("读取的整数: " + num);
            System.out.println("读取的双精度浮点数: " + d);


            raf.seek(0);
            raf.writeUTF("hihihi");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
