package org.talk.is.cheap.project.free.flow.common.task.codec;

public class SimpleStringInputCodec implements InputCodec<String> {

    @Override
    public String encode(String obj) {
        return obj;
    }

    @Override
    public String decode(String encode, Class<String> stringClass) {
        return encode;
    }
}
