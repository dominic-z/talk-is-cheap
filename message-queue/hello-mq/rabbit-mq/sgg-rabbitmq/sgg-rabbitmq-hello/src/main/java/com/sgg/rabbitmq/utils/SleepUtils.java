package com.sgg.rabbitmq.utils;

/**
 * @author dominiczhu
 * @version 1.0
 * @title SleepUtils
 * @date 2022/1/18 12:53 下午
 */
public class SleepUtils {

    public static void sleep(int second) {
        try {
            Thread.sleep(second * 1000L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
