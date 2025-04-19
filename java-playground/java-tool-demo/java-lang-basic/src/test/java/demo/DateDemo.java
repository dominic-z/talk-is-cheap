package demo;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

/**
 * @author dominiczhu
 * @version 1.0
 * @title DateDemo
 * @date 2021/9/2 下午3:09
 */
public class DateDemo {

    @Test
    public void beforeAfter(){
        Calendar startCal = Calendar.getInstance();
        startCal.set(2021, Calendar.SEPTEMBER,19);

        Calendar endCal = Calendar.getInstance();
        endCal.set(2021, Calendar.SEPTEMBER,19);

        System.out.println(startCal.getTime().after(endCal.getTime()));
    }
}
