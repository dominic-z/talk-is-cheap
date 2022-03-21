package demo;

import org.junit.Test;

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
}
