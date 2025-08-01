import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.talk.is.cheap.orm.mybatis.hello.Main;
import org.talk.is.cheap.orm.mybatis.hello.dao.BlogDao;
import org.talk.is.cheap.orm.mybatis.hello.mappers.BlogMapper;

import java.util.List;

@SpringBootTest(classes = Main.class)
@Slf4j
public class TestMybatis {

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private BlogDao blogDao;


    @Test
    public void testMybatis(){
        log.info("selectByIds: {}",blogMapper.selectByIds(List.of(1,3)));
        log.info("selectBlogs: {}",blogMapper.selectBlogs());
        log.info("selectByIds: {}",blogDao.selectByIds(List.of(1,3)));
    }


}
