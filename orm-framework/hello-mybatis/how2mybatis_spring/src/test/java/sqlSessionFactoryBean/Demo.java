package sqlSessionFactoryBean;

import dao.sqlSessionFactoryBean.AuthorMapper;
import dao.sqlSessionFactoryBean.BlogMapper;
import domain.Author;
import domain.Blog;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Demo {
    @Test
    void testXML(){
        ApplicationContext ac=new ClassPathXmlApplicationContext("sqlSessionFactoryBean/spring-config.xml");
        BlogMapper blogMapper=ac.getBean("blogMapper", BlogMapper.class);
        Blog blog = blogMapper.selectById(1);
        System.out.println(blog);

        AuthorMapper authorMapper=ac.getBean("authorMapper", AuthorMapper.class);
        Author author=authorMapper.selectById(1);
        System.out.println(author);
    }

}
