package org.talk.is.cheap.distributed.etcd.java.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.distributed.etcd.java.service.AdvancedEtcdService;

import java.util.Date;

@RestController
@Slf4j
public class LeaseController {

    @Autowired
    AdvancedEtcdService advancedEtcdService;

    @RequestMapping(value = "/lease/{key}/{value}", method = RequestMethod.GET)
    public String lease(@PathVariable("key") String key, @PathVariable("value") String value) throws Exception {
        advancedEtcdService.putWithLease(key, value);
        return "lease success " + new Date();
    }
}
