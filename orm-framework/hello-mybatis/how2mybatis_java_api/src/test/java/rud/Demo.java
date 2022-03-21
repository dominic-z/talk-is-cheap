package rud;

import dao.rud.AuthorMapper;
import dao.rud.BlogMapper;
import domain.Blog;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class Demo {

    Configuration getConfig() throws IOException {
        Properties jdbcProperties = Resources.getResourceAsProperties("jdbc.properties");
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        DataSource dataSource = new PooledDataSource(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/how2mybatis",
                jdbcProperties);
        Environment env = new Environment("production", transactionFactory, dataSource);
        Configuration configuration = new Configuration(env);
//        configuration.setLogImpl(StdOutImpl.class); // 开启日志
        configuration.addMapper(BlogMapper.class);
        configuration.addMapper(AuthorMapper.class);
        return configuration;
    }

    Blog getABlog(){
        Blog blog=new Blog();
        blog.setContent("new content");
        blog.setAuthorId(3);
        blog.setAuthorName("li");
        return blog;
    }
    @Test
    void test() throws IOException {
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        Configuration config = getConfig();
        SqlSessionFactory sqlSessionFactory = builder.build(config);
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);

            List<Blog> blogs;
            blogs=blogMapper.selectBlogs();
            System.out.println(blogs);

            Blog blog=getABlog();
            blogMapper.insertBlog(blog);
            System.out.println(blog);//发现blog的id被成功赋值了

//            还原数据库
            blogMapper.deleteBlogs();
            blogMapper.updateBlogTable();

        }
    }

}