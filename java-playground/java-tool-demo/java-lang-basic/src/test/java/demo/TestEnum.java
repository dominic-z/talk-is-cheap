package demo;

import enums.EA;
import org.junit.Test;

/**
 * @title: TestEnum
 * @Author dominiczhu
 * @Date: 2020/12/8 下午4:24
 * @Version 1.0
 */
public class TestEnum {
    @Test
    public void testValueOf(){
        EA ea1=EA.valueOf("EA1");
        System.out.println(ea1);
    }
}
