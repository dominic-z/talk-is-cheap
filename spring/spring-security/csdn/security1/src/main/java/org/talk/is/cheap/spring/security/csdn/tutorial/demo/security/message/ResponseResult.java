package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseResult<T> {

    private Integer code;
    private String msg;
    private T data;
}
