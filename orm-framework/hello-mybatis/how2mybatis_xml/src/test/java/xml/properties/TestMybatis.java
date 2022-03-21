package xml.properties;

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

public class TestMybatis {
    SqlSessionFactoryBuilder sqlSessionFactoryBuilder=new SqlSessionFactoryBuilder();
    Reader resource;

    {
        try {
            resource = Resources.getResourceAsReader("xml/properties/mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(resource);
    @Test
    void test1(){
        try(SqlSession sqlSession=sqlSessionFactory.openSession()){
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);

            Author author=authorMapper.selectById(1);
            System.out.println(author);

            Blog blog=blogMapper.selectById(1);
            System.out.println(blog);

            blog=sqlSession.selectOne("dao.BlogMapper.selectById",1);
            System.out.println(blog);
        }

        /*结果
        * Author{id=1, name='zhang', age=18.5, sexType=0, sex='MALE'}
Blog{id=1, content='content11', authorId=1, authorName='zhang'}
Blog{id=1, content='content11', authorId=1, authorName='zhang'}*/
    }
}
