package com.example.springboot.hellospringboot.dao.jdbc.template;

import com.example.springboot.hellospringboot.domain.pojo.Customers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ItemDao
 * @date 2021/9/14 下午8:58
 */
@Repository
public class ItemJdbcDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Customers> getCustomersList(){

        List<Customers> customersList=jdbcTemplate.query("select * from customers", (resultSet, i) -> {
            String itemNo = resultSet.getString(2);
            Customers item=new Customers();
            item.setCustomernumber(resultSet.getInt(1));
            return item;
        });

//        do the samething
//        customersList=jdbcTemplate.query("select * from customersList",new BeanPropertyRowMapper<>(Item.class));
        return customersList;
    }

}
