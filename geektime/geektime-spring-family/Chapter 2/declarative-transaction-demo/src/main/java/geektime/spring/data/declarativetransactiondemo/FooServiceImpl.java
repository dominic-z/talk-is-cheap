package geektime.spring.data.declarativetransactiondemo;

import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FooServiceImpl implements FooService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FooService fooService; // 自己注入自己

    @Override
    @Transactional
    public void insertRecord() {
        jdbcTemplate.execute("INSERT INTO FOO (BAR) VALUES ('AAA')");
    }

    @Override
    @Transactional(rollbackFor = RollbackException.class)
    public void insertThenRollback() throws RollbackException {
        jdbcTemplate.execute("INSERT INTO FOO (BAR) VALUES ('BBB')");
        throw new RollbackException();
    }

    @Override
    public void invokeInsertThenRollback() throws RollbackException {

        insertThenRollback();


//      解决方法1
//        ((FooService) AopContext.currentProxy()).insertThenRollback();

//        解决方法2
//        System.out.println(fooService);
//        fooService.insertThenRollback();
    }
}
