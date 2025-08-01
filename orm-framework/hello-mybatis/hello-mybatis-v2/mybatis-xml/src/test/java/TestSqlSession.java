import org.junit.jupiter.api.Test;
import org.talk.is.cheap.orm.mybatis.hello.config.SqlSessionConfig;
import org.talk.is.cheap.orm.mybatis.hello.dao.BlogDao;
import org.talk.is.cheap.orm.mybatis.hello.domain.pojo.Blog;
import org.talk.is.cheap.orm.mybatis.hello.mappers.AuthorMapper;
import org.talk.is.cheap.orm.mybatis.hello.mappers.BlogMapper;

import java.io.IOException;
import java.util.List;


public class TestSqlSession {


    @Test
    public void testBlogDao() {
        BlogDao blogDao = new BlogDao();

        System.out.println(blogDao.selectByIds(List.of(1, 3)));
        System.out.println(blogDao.selectByIds(List.of(1, 3),1,2));
    }


    @Test
    public void testMapper() throws IOException {
        BlogMapper blogMapper = SqlSessionConfig.getMapper(BlogMapper.class);
        System.out.println(blogMapper.selectBlogs());

        Blog newBlog = Blog.builder().authorId(10).authorName("xiaoz").content("a joke").build();
        int insertNum = blogMapper.insert(newBlog);
        System.out.printf("insert num: %d, new blog id: %d %n", insertNum, newBlog.getId());

        List<Blog> blogs = blogMapper.selectByIds(List.of(newBlog.getId()));
        System.out.printf("select by list: %s %n", blogs);

        int deleteNum = blogMapper.deleteById(newBlog.getId());
        System.out.printf("delete num: %d %n", deleteNum);


        System.out.printf("map key: %s %n", blogMapper.selectBlogsByAuthorId(1));

    }


    @Test
    public void testTypeHandler() throws IOException {
        BlogMapper blogMapper = SqlSessionConfig.getMapper(BlogMapper.class);
        System.out.println(blogMapper.selectById(2));
    }

    @Test
    public void testConstructor() throws IOException {
        BlogMapper blogMapper = SqlSessionConfig.getMapper(BlogMapper.class);
        System.out.println(blogMapper.selectByIdWithConstructor(2));
    }


    @Test
    public void testAssociation() throws IOException {
        AuthorMapper authorMapper = SqlSessionConfig.getMapper(AuthorMapper.class);
        System.out.println(authorMapper.selectByIdWithCollection(1));


        BlogMapper blogMapper = SqlSessionConfig.getMapper(BlogMapper.class);
        System.out.println(blogMapper.selectByIdWithAssociation(1));
    }
}
