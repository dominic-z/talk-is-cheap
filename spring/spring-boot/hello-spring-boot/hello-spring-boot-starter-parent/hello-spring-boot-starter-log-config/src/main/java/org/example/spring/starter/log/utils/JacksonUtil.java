package org.example.spring.starter.log.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author dominiczhu
 * @version 1.0
 * @title JacksonUtil
 * @date 2022/2/14 1:40 下午
 */
public class JacksonUtil {
    public static ObjectMapper MAPPER = new ObjectMapper();

    private JacksonUtil(){

    }
}
