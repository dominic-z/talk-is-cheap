package mapper.parameter;

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
import java.util.Arrays;
import java.util.List;

public class TestMyBatis {
    SqlSessionFactoryBuilder sqlSessionFactoryBuilder=new SqlSessionFactoryBuilder();
    Reader resource;

    {
        try {
            resource = Resources.getResourceAsReader("mapper/parameter/mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(resource);

    Author getAAuthor(){
        Author author=new Author();
        author.setId(1);
        author.setName("zhang");
//        author.setAge(18.5f);
//        author.setSex("MALE");
//        author.setSexType(0);
        return author;
    }
    @Test
    void test1(){
        try(SqlSession sqlSession=sqlSessionFactory.openSession()){
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);

            Author author=getAAuthor();
//            System.out.println(author);
            Author resAuthor;

            resAuthor=authorMapper.selectByName("zhang");
            System.out.println(resAuthor);
            resAuthor=authorMapper.selectByAuthorBean(author);
            System.out.println(resAuthor);

            resAuthor=authorMapper.selectByArgs(1,"zhang");
            System.out.println(resAuthor);

            List<Author> authors=authorMapper.selectAllAuthorsOrderBy("id");
            System.out.println(authors);

            resAuthor = authorMapper.selectByTableAndId("author",1);
            System.out.println(resAuthor);

            AuthorName authorName=new AuthorName();
            authorName.setName("li");
            resAuthor=authorMapper.selectByAuthorName(authorName);
            System.out.println(resAuthor);

//            需要在设置里开启-parameters参数，然后rebuild重新编译
            resAuthor=authorMapper.selectByAuthorNameAndId(authorName,3);
            System.out.println(resAuthor);

            /*执行结果
Author{id=1, name='zhang', age=18.5, sexType=0, sex='MALE'}
Author{id=1, name='zhang', age=18.5, sexType=0, sex='MALE'}
Author{id=1, name='zhang', age=18.5, sexType=0, sex='MALE'}
[Author{id=3, name='li', age=21.7, sexType=1, sex='FEMALE'}, Author{id=2, name='wang', age=21.2, sexType=1, sex='FEMALE'}, Author{id=1, name='zhang', age=18.5, sexType=0, sex='MALE'}]
Author{id=1, name='zhang', age=18.5, sexType=0, sex='MALE'}
调用setNonNullParameter方法
Author{id=3, name='li', age=21.7, sexType=1, sex='FEMALE'}
调用setNonNullParameter方法
Author{id=3, name='li', age=21.7, sexType=1, sex='FEMALE'}
* */
        }
    }
}
