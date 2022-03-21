package autowired;

import config.SpringConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author dominiczhu
 * @version 1.0
 * @title autowiredTest
 * @date 2021/3/23 下午7:41
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfig.class})
public class SomeUtilTest {

    @Test
    public void testSomeUtil(){
        SomeUtil.show();
        SomeUtil.useService();
    }
}
