package gettingStarted;

import dao.gettingStarted.BlogMapper;
import domain.Blog;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import springConfig.gettingStarted.SpringConfig;

public class Demo {
    @Test
    void testXML(){
        ApplicationContext ac=new ClassPathXmlApplicationContext("gettingStarted/spring-config.xml");
        BlogMapper blogMapper=ac.getBean("blogMapper", BlogMapper.class);
        Blog blog = blogMapper.selectById(1);
        System.out.println(blog);
    }

    @Test
    void testAnnotation(){
        ApplicationContext ac=new AnnotationConfigApplicationContext(SpringConfig.class);
        BlogMapper blogMapper1=ac.getBean("blogMapper1", BlogMapper.class);
        Blog blog1 = blogMapper1.selectById(1);
        System.out.println(blog1);

        BlogMapper blogMapper2=ac.getBean("blogMapper2", BlogMapper.class);
        Blog blog2 = blogMapper2.selectById(2);
        System.out.println(blog2);
    }
}
