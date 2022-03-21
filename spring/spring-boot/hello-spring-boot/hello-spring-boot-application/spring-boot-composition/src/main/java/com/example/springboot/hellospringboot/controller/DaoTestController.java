package com.example.springboot.hellospringboot.controller;

import com.example.springboot.hellospringboot.domain.messages.HelloRequest;
import com.example.springboot.hellospringboot.domain.messages.HelloResponse;
import com.example.springboot.hellospringboot.domain.pojo.Customers;
import com.example.springboot.hellospringboot.service.CustomersService;
import com.example.springboot.hellospringboot.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dominiczhu
 * @version 1.0
 * @title DaoTestController
 * @date 2022/2/13 5:34 下午
 */
@RestController()
@RequestMapping(path = "/web/daoTest")
@Slf4j
public class DaoTestController {


    @Autowired
    private CustomersService customersService;

    @ResponseBody
    @RequestMapping(path = "/selectByPrimaryKey", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Customers selectByPrimaryKey(@RequestParam("id") int id) {
        log.debug("1232");

        return customersService.selectByPrimaryKey(id);
    }
}
