package com.example.springboot.hellospringboot;

import com.example.springboot.hellospringboot.service.CustomersService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author dominiczhu
 * @version 1.0
 * @title DaoTest
 * @date 2022/1/25 10:47 上午
 */

@SpringBootTest(classes = HelloSpringBootApplication.class)
//@RunWith(SpringRunner.class) // 没搞清楚这东西有啥用
@Slf4j(topic = "root")
public class DaoTest {

    @Autowired
    private CustomersService customersService;

    @Test
    public void testTransaction() throws Exception {
        customersService.testInnerCallTransaction();
    }


    @Test
    public void testSimpleSql(){
        log.debug("1232");
        customersService.selectByPrimaryKey(119);
    }

}
