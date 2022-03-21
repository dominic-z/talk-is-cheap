package enums;

import org.junit.Test;

/**
 * @title Demo
 * @Author dominiczhu
 * @Date 2020/12/25 上午11:14
 * @Version 1.0
 */
public class Demo {
    @Test
    public void valueOf() {
        EA ea = EA.valueOf("EA1");
        System.out.println(ea);
    }
}
