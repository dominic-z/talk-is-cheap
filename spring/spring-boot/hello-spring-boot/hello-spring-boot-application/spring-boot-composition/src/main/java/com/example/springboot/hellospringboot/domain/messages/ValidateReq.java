package com.example.springboot.hellospringboot.domain.messages;

import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ValidateReq
 * @date 2022/1/25 9:18 上午
 */
@Data
public class ValidateReq {

    @Max(value = 20)
    private int i;

    @NotNull(message = "s不可以为空")
    private String s;

    @NotEmpty(message = "stringList不可以为空")
    private List<String> stringList;

    @AssertTrue(message = "b必须为true")
    private boolean b;

}
