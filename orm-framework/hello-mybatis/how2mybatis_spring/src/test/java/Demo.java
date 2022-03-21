import dao.BlogMapper;
import domain.Blog;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Demo {
    @Test
    void test(){
        ApplicationContext ac=new ClassPathXmlApplicationContext("spring-config.xml");
        BlogMapper blogMapper=ac.getBean("blogMapper", BlogMapper.class);
        Blog blog = blogMapper.selectById(1);
        System.out.println(blog);
    }
}
