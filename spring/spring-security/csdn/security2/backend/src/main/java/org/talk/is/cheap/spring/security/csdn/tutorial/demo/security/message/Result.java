package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public  static <K>  Result<K> ok(K data){
        Result<K> kResult = new Result<>(0,"",data);
        return kResult;
    }
}
