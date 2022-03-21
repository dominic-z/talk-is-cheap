package aware;

import com.example.HelloSpringApplication;
import com.example.aware.InsertItselfDemo;
import com.example.components.Apple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @title ApplicationAwareTest
 * @Author dominiczhu
 * @Date 2020/12/22 下午2:21
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {HelloSpringApplication.class})//指定配置类
public class InsertItselfTest {
    @Autowired
    @Qualifier("insertItself")
    InsertItselfDemo insertItself;

    @Test
    public void testMethodInject() {
        System.out.println(insertItself.getAc());
        Apple apple = insertItself.getAc().getBean("apple", Apple.class);
        System.out.println(apple);
        insertItself.sayHello();
    }
}
