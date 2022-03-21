package mapperScan;

import dao.mapperScan.BlogMapper;
import domain.Blog;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import springConfig.mapperScan.SpringConfig;

public class Demo {
    @Test
    void testXML() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("mapperScan/spring-config.xml");
        BlogMapper blogMapper = ac.getBean("blogMapper", BlogMapper.class);
        Blog blog = blogMapper.selectById(1);
        System.out.println(blog);
    }

    @Test
    void testAnnotation() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(SpringConfig.class);
        BlogMapper blogMapper = ac.getBean("blogMapper", BlogMapper.class);
        Blog blog = blogMapper.selectById(1);
        System.out.println(blog);
    }
}
