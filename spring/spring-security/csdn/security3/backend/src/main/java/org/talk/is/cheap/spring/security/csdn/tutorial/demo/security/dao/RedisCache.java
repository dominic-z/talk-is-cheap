package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Repository;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JsonUtil;

import java.util.Collection;
import java.util.HashMap;

@Repository
public class RedisCache extends HashMap<String,String> {

    public <T> void setCacheObject(String key,T value) throws JsonProcessingException {
        this.put(key, JsonUtil.toJsonString(value));
    }

    public <T> T getCacheObject(String key,Class<T> tClass) throws JsonProcessingException {
        String value = this.get(key);
        if(value!=null){
            return JsonUtil.fromJsonString(value,tClass);
        }
        return null;
    }

    public void deleteObject(String key){
        this.remove(key);
    }

    public void deleteObject(Collection<String> keys){
        keys.forEach(this::deleteObject);
    }
}