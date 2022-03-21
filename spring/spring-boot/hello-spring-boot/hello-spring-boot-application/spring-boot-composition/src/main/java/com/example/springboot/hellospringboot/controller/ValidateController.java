package com.example.springboot.hellospringboot.controller;

import com.example.springboot.hellospringboot.domain.messages.ValidateReq;
import com.example.springboot.hellospringboot.domain.messages.ValidateResp;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.util.validation.ValidationError;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ValidateController
 * @date 2022/1/25 9:29 上午
 */
@Slf4j
@RestController
@RequestMapping(path = "/web/validate")
// 如果加上了这个注解，参数异常的代码会直接抛出异常，否则会仍然进入到方法里。可以从bindingResult获取异常信息
//@Validated
public class ValidateController {

    private static final String LOG_MARK = "[ValidateController]";

    @RequestMapping(value = "/validateReq", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ValidateResp validateReq(@RequestBody @Valid ValidateReq req, BindingResult bindingResult) {

        log.info("{} req {} {}", LOG_MARK, req, bindingResult);
        if (bindingResult != null && bindingResult.hasErrors()) {
            log.error("{} errors: {}", LOG_MARK, bindingResult.getAllErrors());
            // 返回 校验校验错误提示信息
        }
        final ValidateResp resp = new ValidateResp();
        resp.setCode(1);
        return resp;
    }

}
