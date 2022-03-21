package dynamicSql.dynamicBind;

import dao.AuthorMapper;
import dao.BlogMapper;
import domain.Author;
import domain.Blog;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;
import wiredDomain.AuthorName;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMyBatis {
    SqlSessionFactoryBuilder sqlSessionFactoryBuilder=new SqlSessionFactoryBuilder();
    Reader resource;

    {
        try {
            resource = Resources.getResourceAsReader("dynamicSql/dynamicBind/mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(resource);

    List<Integer> getIdList(){
        List<Integer> idList=new ArrayList<>();
        idList.add(1);idList.add(2);
        return idList;
    }
    Map<Integer,Author> getAuthorIdAuthorMap(){
        Map<Integer,Author> authorIdAuthorMap=new HashMap<>();
        Author author=new Author();
        author.setId(1);
        author.setName("zhang");
        authorIdAuthorMap.put(1,author);
        author=new Author();
        author.setId(2);
        author.setName("wang");
        authorIdAuthorMap.put(2,author);
        return authorIdAuthorMap;
    }
    @Test
    void test1(){
        try(SqlSession sqlSession=sqlSessionFactory.openSession()){
            AuthorMapper authorMapper=sqlSession.getMapper(AuthorMapper.class);

            AuthorName authorName=new AuthorName();
            authorName.setName("zhang");
            Author resAuthor = authorMapper.selectByAuthorNameObj(authorName);
            System.out.println(resAuthor);

            /*执行结果
Author{id=1, name='zhang', age=18.5, sexType=0, sex='MALE'}
* */
        }
    }
}
