package com.example.springboot.hellospringboot.controller;

import com.example.springboot.hellospringboot.domain.pojo.Customers;
import com.example.springboot.hellospringboot.service.CustomersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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


    @ResponseBody
    @RequestMapping(path = "/testTransaction", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> testTransaction(){
        HashMap<String, Object> map = new HashMap<>();
        try {
            customersService.testTransactionPropagation();
            map.put("msg","done");
        } catch (Exception e) {
            map.put("msg",e.getMessage());
        }
        return map;
    }
}
