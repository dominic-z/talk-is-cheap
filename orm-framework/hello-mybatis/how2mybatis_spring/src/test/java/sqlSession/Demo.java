package sqlSession;

import dao.sqlSession.AuthorMapper;
import dao.sqlSession.BlogMapper;
import domain.Author;
import domain.Blog;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Demo {
    @Test
    void testXML() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("sqlSession/spring-config.xml");
        SqlSession sqlSession = ac.getBean("sqlSession", SqlSession.class);

        BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);
        Blog blog = blogMapper.selectById(1);
        System.out.println(blog);

        AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
        Author author = authorMapper.selectById(1);
        System.out.println(author);
    }

}
