package org.talk.is.cheap.hello.druid.spring.boot.starter.service;


import com.alibaba.druid.pool.DruidDataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
@Slf4j
public class DBService {

    @Autowired
    DruidDataSource druidDataSource;

    @Autowired
    @Getter
    JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    public void simpleTx() throws SQLException {

        jdbcTemplate.execute("INSERT INTO `tokens` VALUES ('ccccc');");
//        throw new RuntimeException("rollback");

    }


    @Autowired
    private ApplicationContext applicationContext;

    /**
     * spring创建的bean是代理对象，因此如果直接通过this调用方法，并不会触发事务管理器的aop方法启动事务以及回滚，这是个老生常谈的问题
     * 要想用，就得通过applicationContext获取这个对象的代理对象。
     * 但还有另外一个坑，spring并不会对private方法进行aop， https://www.doubao.com/thread/wc3c85a60086fc4e2
     * 也就是说，@Transactional(rollbackFor = Exception.class)挂在private方法上也没用，必须是public方法
     */
//    @Transactional(rollbackFor = Exception.class) // 当然如果直接在这个方法上套个transactional注解，那也没问题
    public void outerCall() {
        try {
            privateInnerCall();
        } catch (Exception e) {
            log.error("privateInnerCall: ", e);
        }


        try {
            // 正确用法
            DBService proxy = applicationContext.getBean(DBService.class);
            proxy.publicInnerCall(proxy);
        } catch (Exception e) {
            log.error("publicInnerCall: ", e);
        }

    }


    @Transactional(rollbackFor = Exception.class)
    private void privateInnerCall() {
        jdbcTemplate.execute("INSERT INTO `tokens` VALUES ('eeee');");
        throw new RuntimeException("rollback");
    }

    @Transactional(rollbackFor = Exception.class)
    public void publicInnerCall(DBService proxy) {
        // 必须得通过get方法获取，因为这个DBService实际是个proxy对象，他并没有jdbcTemplate这个成员变量，因此需要原本的DBService提供一个getter方法
        // 这样proxy对象才能通过代理这个getter方法，获取jdbcTemplate对象。如果需要依赖多个其他bean，就非常麻烦，因此，除非必要，不要这么整。
        proxy.getJdbcTemplate().execute("INSERT INTO `tokens` VALUES ('dddddd');");
        throw new RuntimeException("rollback");
    }
}
