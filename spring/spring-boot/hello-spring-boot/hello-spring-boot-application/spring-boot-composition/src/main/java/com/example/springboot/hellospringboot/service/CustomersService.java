package com.example.springboot.hellospringboot.service;

import com.example.springboot.hellospringboot.dao.mbg.CustomersMapper;
import com.example.springboot.hellospringboot.dao.customized.CustomersDao;
import com.example.springboot.hellospringboot.domain.pojo.Customers;
import com.example.springboot.hellospringboot.domain.query.example.CustomersExample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author codegen
 * @date 2022/01/14
 */
@Service
@Slf4j
public class CustomersService {

    @Autowired
    private CustomersDao customersDao;

    @Autowired
    private CustomersMapper customersMapper;


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void testTransactionPropagation() {
        final Customers customer498 = customersMapper.selectByPrimaryKey(496);
        customer498.setCustomernumber(498);
        customer498.setCity("new york");
        customersMapper.insert(customer498);

        try {
            int i = testTransactionPropagationInner(customer498);
        } catch (Exception e) {
            log.error("error: ", e);
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public int testTransactionPropagationInner(Customers record) {
        record.setCustomernumber(record.getCustomernumber() + 1);
        record.setCity("taiwan");
        int insert = customersMapper.insert(record);
        int i = 0;
        if (i == 0) {
            throw new RuntimeException("抛出异常 尝试回滚");
        }
        return insert;
    }

    public int testInnerCallTransaction() throws Exception {
        return createTwo();
    }

    // 内部调用事务失效，猜测，本service并不是接口实现类，但内部调用的时候仍然事务不生效
    // 猜测是在外部调用createTwo的地方做了字节码aop，而并不是在本类的基础上做aop
    @Transactional(rollbackFor = Exception.class)
    public int createTwo() {
        final Customers customer496 = customersMapper.selectByPrimaryKey(496);
        customer496.setCustomernumber(498);
        create(customer496);
        int i = 0;
        if (i == 0) {
            throw new RuntimeException("抛出异常 尝试回滚");
        }
        customer496.setCustomernumber(499);
        create(customer496);
        return 2;
    }

    // 基于CustomersMapper

    @Transactional(rollbackFor = Exception.class)
    public int create(Customers record) {
        if (record == null) {
            return 0;
        }
        return customersMapper.insert(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public int createBatch(Collection<Customers> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return customersMapper.insertBatch(records);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryKey(Integer key) {
        if (key == null) {
            return 0;
        }
        return customersMapper.deleteByPrimaryKey(key);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByPrimaryKey(Customers record) {
        if (record == null) {
            return 0;
        }
        return customersMapper.updateByPrimaryKey(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByExampleSelective(Customers record, CustomersExample example) {
        if (record == null || example == null) {
            return 0;
        }
        return customersMapper.updateByExampleSelective(record, example);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByExample(Customers record, CustomersExample example) {
        if (record == null || example == null) {
            return 0;
        }
        return customersMapper.updateByExample(record, example);
    }


    public long countByExample(CustomersExample example) {
        if (example == null) {
            return 0L;
        }

        return customersMapper.countByExample(example);
    }

    public List<Customers> selectByExample(CustomersExample example) {
        if (example == null) {
            return new ArrayList<>();
        }
        return customersMapper.selectByExample(example);
    }

    public Customers selectByPrimaryKey(Integer key) {
        if (key == null) {
            return null;
        }
        return customersMapper.selectByPrimaryKey(key);
    }

    // 基于customersDao

}
