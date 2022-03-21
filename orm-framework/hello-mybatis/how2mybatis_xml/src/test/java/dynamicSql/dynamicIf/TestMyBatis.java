package dynamicSql.dynamicIf;

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

public class TestMyBatis {
    SqlSessionFactoryBuilder sqlSessionFactoryBuilder=new SqlSessionFactoryBuilder();
    Reader resource;

    {
        try {
            resource = Resources.getResourceAsReader("dynamicSql/dynamicIf/mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(resource);

    @Test
    void test1(){
        try(SqlSession sqlSession=sqlSessionFactory.openSession()){
            BlogMapper blogMapper=sqlSession.getMapper(BlogMapper.class);

            List<Blog> blogList;
            blogList=blogMapper.selectBlogsByIf(null,null);
            System.out.println(blogList);

            blogList=blogMapper.selectBlogsByIf("content11",null);
            System.out.println(blogList);

            Author author=new Author();
            author.setName("li");
            blogList=blogMapper.selectBlogsByIf(null,author);
            System.out.println(blogList);

            /*执行结果
[Blog{id=1, content='content11', authorId=1, authorName='zhang'}, Blog{id=2, content='content12', authorId=1, authorName='zhang'}, Blog{id=3, content='content2', authorId=2, authorName='wang'}, Blog{id=4, content='content3', authorId=3, authorName='li'}]
[Blog{id=1, content='content11', authorId=1, authorName='zhang'}]
[Blog{id=4, content='content3', authorId=3, authorName='li'}]
* */
        }
    }
}
