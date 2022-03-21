package mapper.resultMap;

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
import java.util.List;

public class TestMyBatis {
    SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
    Reader resource;

    {
        try {
            resource = Resources.getResourceAsReader("mapper/resultMap/mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(resource);

    @Test
    void test1() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);

            Blog resBlog;
            resBlog = blogMapper.selectById(1);
            System.out.println(resBlog);
            System.out.println(resBlog.getAuthorNameObj());

            resBlog = blogMapper.selectByIdWithConstructor(1);
            System.out.println(resBlog);

            resBlog = blogMapper.selectByIdWithOnlyConstructor(1);
            System.out.println(resBlog);
            resBlog=blogMapper.selectByIdWithAssociation(1);
            System.out.println(resBlog.getAuthorObj());

            resBlog=blogMapper.selectByIdWithNestedSelectAssociation(1);
            System.out.println(resBlog.getAuthorObj());

            resBlog=blogMapper.selectByIdWithNestedResultsAssociation(1);
            System.out.println(resBlog.getAuthorObj());

            List<Blog> blogList;
            blogList=blogMapper.selectAllBlogsWithNestedSelectAssociation();
            System.out.println(blogList);
            System.out.println(blogList.get(0).getAuthorObj()==blogList.get(1).getAuthorObj());

            blogList=blogMapper.selectAllBlogsWithNestedResultsAssociation();
            System.out.println(blogList);
            System.out.println(blogList.get(0).getAuthorObj()==blogList.get(1).getAuthorObj());

            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            Author resAuthor;
            resAuthor = authorMapper.selectByIdWithCollection(1);
            System.out.println(resAuthor.getBlogList());

            resAuthor = authorMapper.selectByIdWithNestedSelectColleciton(2);
            System.out.println(resAuthor.getBlogList());

            List<Author> authorList;
            authorList = authorMapper.selectAuthorsByIdWithNestedSelectColleciton();
            System.out.println(authorList.get(0).getBlogList().get(0)==authorList.get(1).getBlogList().get(0));

            resAuthor=authorMapper.selectByIdWithDiscriminator(1);
            System.out.println(resAuthor);
            resAuthor=authorMapper.selectByIdWithDiscriminator(2);
            System.out.println(resAuthor);

            resAuthor=authorMapper.selectByIdWithDiscriminatorExtended(1);
            System.out.println(resAuthor);
            /*执行结果
通过列名调用getNullableResult
Blog{id=1, content='content11', authorId=1, authorName='zhang'}
我是：AuthorName{name='zhang'}
带参构造函数调用
Blog{id=1, content='content11', authorId=1, authorName='zhang'}
带参构造函数调用
Blog{id=1, content='content11', authorId=1, authorName='zhang'}
Author{id=1, name='zhang', age=0.0, sexType=0, sex='null'}
Author{id=1, name='zhang', age=18.5, sexType=0, sex='MALE'}
Author{id=1, name='zhang', age=0.0, sexType=0, sex='null'}
[Blog{id=1, content='content11', authorId=0, authorName='zhang'}, Blog{id=2, content='content12', authorId=0, authorName='zhang'}, Blog{id=3, content='content2', authorId=0, authorName='wang'}, Blog{id=4, content='content3', authorId=0, authorName='li'}]
true
[Blog{id=1, content='content11', authorId=0, authorName='null'}, Blog{id=2, content='content12', authorId=0, authorName='null'}, Blog{id=3, content='content2', authorId=0, authorName='null'}, Blog{id=4, content='content3', authorId=0, authorName='null'}]
false
[Blog{id=1, content='content11', authorId=1, authorName='zhang'}, Blog{id=2, content='content12', authorId=1, authorName='zhang'}]
[Blog{id=3, content='content2', authorId=2, authorName='wang'}]
false
Author{id=0, name='zhang', age=0.0, sexType=0, sex='null'} //name被赋值
Author{id=0, name='null', age=21.2, sexType=0, sex='null'} //age被赋值
Author{id=1, name='null', age=18.5, sexType=0, sex='null'}
* */
        }
    }
}
