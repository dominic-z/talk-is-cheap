package mapper.insert;

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
    SqlSessionFactoryBuilder sqlSessionFactoryBuilder=new SqlSessionFactoryBuilder();
    Reader resource;

    {
        try {
            resource = Resources.getResourceAsReader("mapper/insert/mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(resource);

    Blog getABlog(){
        Blog blog=new Blog();
        blog.setContent("new content");
        blog.setAuthorId(3);
        blog.setAuthorName("li");
        return blog;
    }
    @Test
    void test1(){
        try(SqlSession sqlSession=sqlSessionFactory.openSession()){
            BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);

            Blog blog=getABlog();
            System.out.println(blog);

            blog=getABlog();
            blogMapper.insertABlog(blog);
            System.out.println(blog);

            blog=getABlog();
            blogMapper.insertABlogReturnKey(blog);
            System.out.println(blog);

            List<Blog> blogs=new ArrayList<>();
            Blog blog1=getABlog();
            blogs.add(blog1);
            Blog blog2=getABlog();
            blogs.add(blog2);
            blogMapper.insertBlogList(blogs);
            System.out.println(blogs);

            Blog[] blogsArr=new Blog[]{blog1,blog2};
            blogMapper.insertBlogArr(blogsArr);
            System.out.println(Arrays.toString(blogsArr));
            sqlSession.commit();//增加操作要手动commit

            /*执行结果
Blog{id=0, content='new content', authorId=3, authorName='li'}
Blog{id=0, content='new content', authorId=3, authorName='li'}
Blog{id=6, content='new content', authorId=3, authorName='li'}
[Blog{id=7, content='new content', authorId=3, authorName='li'}, Blog{id=8, content='new content', authorId=3, authorName='li'}]
[Blog{id=9, content='new content', authorId=3, authorName='li'}, Blog{id=10, content='new content', authorId=3, authorName='li'}]

实验完之后记得把数据库恢复原样，以便后续实验
delete from how2mybatis.blog where id>=5;
ALTER TABLE how2mybatis.blog auto_increment=5;
* */
        }
    }
}
