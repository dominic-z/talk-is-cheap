package mapper.sql;

import dao.AuthorMapper;
import dao.BlogMapper;
import domain.Author;
import domain.Blog;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestMyBatis {
    SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
    Reader resource;

    {
        try {
            resource = Resources.getResourceAsReader("mapper/sql/mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(resource);

    Blog getABlog() {
        Blog blog = new Blog();
        blog.setContent("new content");
        blog.setAuthorId(3);
        blog.setAuthorName("li");
        return blog;
    }

    @Test
    void test1() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            Author author = authorMapper.selectByIdWithSimpleSql(1);
            System.out.println(author);

            author = authorMapper.selectByIdWithNestedSql(1);
            System.out.println(author);

            /*执行结果
Author{id=1, name='zhang', age=18.5, sexType=0, sex='null'}
Author{id=1, name='zhang', age=18.5, sexType=0, sex='MALE'}
* */
        }
    }
}
