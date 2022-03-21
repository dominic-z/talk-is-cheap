package dynamicSql.dynamicForeach;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMyBatis {
    SqlSessionFactoryBuilder sqlSessionFactoryBuilder=new SqlSessionFactoryBuilder();
    Reader resource;

    {
        try {
            resource = Resources.getResourceAsReader("dynamicSql/dynamicForeach/mybatis-config.xml");
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
            BlogMapper blogMapper=sqlSession.getMapper(BlogMapper.class);

            List<Blog> blogList;
            List<Integer> idList=getIdList();
            blogList=blogMapper.selectBlogsByIdList(idList);
            System.out.println(blogList);

            Map<Integer,Author> authorIdAuthorMap=getAuthorIdAuthorMap();
            blogList=blogMapper.selectBlogsByAuthorMapId(authorIdAuthorMap);
            System.out.println(blogList);
            blogList=blogMapper.selectBlogsByAuthorMapName(authorIdAuthorMap);
            System.out.println(blogList);

            /*执行结果
[Blog{id=1, content='content11', authorId=1, authorName='zhang'}, Blog{id=2, content='content12', authorId=1, authorName='zhang'}]
[Blog{id=1, content='content11', authorId=1, authorName='zhang'}, Blog{id=2, content='content12', authorId=1, authorName='zhang'}, Blog{id=3, content='content2', authorId=2, authorName='wang'}]
[Blog{id=1, content='content11', authorId=1, authorName='zhang'}, Blog{id=2, content='content12', authorId=1, authorName='zhang'}, Blog{id=3, content='content2', authorId=2, authorName='wang'}]
* */
        }
    }
}
