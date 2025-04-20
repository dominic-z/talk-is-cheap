package demo;

import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Number
 * @date 2021/8/6 上午10:39
 */
public class Number {


    @Test
    public void round(){
        double d = 114.145;
        d = (double) Math.round(d * 100) / 100;
        System.out.println(d);

        BigDecimal b = new BigDecimal(d);
        d = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        System.out.println(d);
    }


    public static void main(String[] args) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("output.txt"), true)) {
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
}
