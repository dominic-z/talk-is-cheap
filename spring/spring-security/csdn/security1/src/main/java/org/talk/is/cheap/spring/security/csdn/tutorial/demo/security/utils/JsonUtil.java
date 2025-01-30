package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static  <T> String toJsonString(T obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T fromJsonString(String s, Class<T> tClass) throws JsonProcessingException {
        return objectMapper.readValue(s,tClass);
    }

}
