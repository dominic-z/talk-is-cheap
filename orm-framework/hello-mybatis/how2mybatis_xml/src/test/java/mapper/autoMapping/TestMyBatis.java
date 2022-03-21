package mapper.autoMapping;

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

public class TestMyBatis {
    SqlSessionFactoryBuilder sqlSessionFactoryBuilder=new SqlSessionFactoryBuilder();
    Reader resource;

    {
        try {
            resource = Resources.getResourceAsReader("mapper/autoMapping/mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(resource);

    @Test
    void test1(){
        try(SqlSession sqlSession=sqlSessionFactory.openSession()){
            BlogMapper blogMapper=sqlSession.getMapper(BlogMapper.class);
            Blog resBlog;
            resBlog=blogMapper.selectToShowRiskOfAutoMapping(1);
            System.out.println(resBlog);
            System.out.println(resBlog.getAuthorObj());
            /*执行结果
Blog{id=1, content='content11', authorId=0, authorName='null'}
Author{id=1, name='zhang', age=0.0, sexType=0, sex='null'}
* */
        }
    }
}
