package org.example.backend;


import lombok.extern.slf4j.Slf4j;
import org.example.backend.config.ConfigFromNacos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = App.class)
@Slf4j
public class Testing {

    @Autowired
    ConfigFromNacos ConfigFromNacos;
    @Test
    public void testNacosConfig(){
        log.info(ConfigFromNacos.getInfo1());
        log.info(ConfigFromNacos.getInfo2());

    }
}
