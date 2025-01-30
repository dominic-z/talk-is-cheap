package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.message.resp;

import lombok.Builder;
import lombok.Data;

@Data
//@Builder
public class Result<T> {
    private int code;
    private String message;
    private T data;
}
