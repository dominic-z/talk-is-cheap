package org.talk.is.cheap.hello.druid.spring.boot.starter.service;


import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@Slf4j
public class DBService {

    @Autowired
    DruidDataSource druidDataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    public void simpleTx() throws SQLException {

        jdbcTemplate.execute("INSERT INTO `tokens` VALUES ('ccccc');");
//        throw new RuntimeException("rollback");

    }

}
