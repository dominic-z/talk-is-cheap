package org.talk.is.cheap.web.cors.demo.message;

import lombok.Data;

@Data
public class Response<T> {

    private int code;
    private String message;
    private T data;

    public static <K> Response<K> OK(K data) {
        Response<K> resp = new Response<>();
        resp.setCode(0);
        resp.setData(data);
        return resp;
    }
}
