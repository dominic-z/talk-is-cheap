package org.talk.is.cheap.project.free.flow.common.task.definition.codec;

import com.google.gson.Gson;

public class JsonInputCodec<T> implements InputCodec<T> {

    private final Gson gson = new Gson();


    @Override
    public String encode(T obj) {
        return gson.toJson(obj);
    }

    @Override
    public T decode(String encode, Class<T> tClass) {
        return gson.fromJson(encode, tClass);
    }


}
