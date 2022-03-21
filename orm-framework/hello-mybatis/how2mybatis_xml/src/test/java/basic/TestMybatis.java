package basic;

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
import java.util.List;

public class TestMybatis {
    @Test
    void test1() throws IOException {
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        Reader resource = Resources.getResourceAsReader("basic/mybatis-basic-config.xml");
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(resource);
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);

            Author author = authorMapper.selectById(1);
            System.out.println(author);

            Blog blog = blogMapper.selectById(1);
            System.out.println(blog);

            List<Blog> blogs = blogMapper.selectAllBlogs();
            System.out.println(blogs);

            blog = sqlSession.selectOne("dao.BlogMapper.selectById", 1);
            System.out.println(blog);
        }

        /* 运行结果
Author{id=1, name='zhang', age=18.5, sexType=0, sex='MALE'}
Blog{id=1, content='content11', authorId=1, authorName='zhang'}
[Blog{id=1, content='content11', authorId=1, authorName='zhang'}, Blog{id=2, content='content12', authorId=1, authorName='zhang'}, Blog{id=3, content='content2', authorId=2, authorName='wang'}, Blog{id=4, content='content3', authorId=3, authorName='li'}]
Blog{id=1, content='content11', authorId=1, authorName='zhang'}
        * */
    }
}
