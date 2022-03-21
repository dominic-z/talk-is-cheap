package com.example.springboot.hellospringboot.dao.customized;

import com.example.springboot.hellospringboot.domain.pojo.Customers;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for customized sql defined in  customized/mapper.xml
 * for example
 * public List<Customers> findByIds(Collection<Long> ids) {
 * if (ids.isEmpty()) {
 * return new ArrayList<>();
 * }
 * Map<String, Object> params = new HashMap<>();
 * params.put("ids", ids);
 * return sqlSessionTemplate.selectList(TABLE + ".select_by_ids", params);
 * }
 *
 * @author codegen
 * @date 2022/01/14
 */
@Component
public class CustomersDao {

    private static final String TABLE = "com.example.springboot.hellospringboot.dao.customized.CustomersDao";

    @Autowired
    @Qualifier("yiibaiSqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    public List<Customers> selectCustomersRowBounds(int start, int end) {
        return sqlSessionTemplate.selectList(TABLE + ".selectCustomersRowBounds", new HashMap<>(),
                new RowBounds(start, end));
    }

    public Customers selectByCustomerNumber(int customerNumber) {
        Map<String, Object> params = new HashMap<>();
        params.put("customerNumber", customerNumber);
        return sqlSessionTemplate.selectOne(TABLE + ".selectByCustomerNumber", params);

    }

}
