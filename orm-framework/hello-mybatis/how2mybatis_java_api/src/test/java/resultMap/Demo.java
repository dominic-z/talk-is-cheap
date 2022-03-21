package resultMap;

import dao.resultMap.AuthorMapper;
import dao.resultMap.BlogMapper;
import domain.Author;
import domain.Blog;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
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

    Blog getABlog() {
        Blog blog = new Blog();
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

            Blog blog;
            blog = blogMapper.selectById(1);
            System.out.println(blog);
            System.out.println(blog.getAuthorNameObj());

            blog = blogMapper.selectByIdWithConstructor(1);
            System.out.println(blog);

            blog = blogMapper.selectByIdWithAssociation(1);
            Author author1=blog.getAuthorObj();
            System.out.println(author1);
            blog = blogMapper.selectByIdWithAssociation(2);
            Author author2=blog.getAuthorObj();
            System.out.println(author1);
            System.out.println(author1==author2);

            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            Author author;
            author=authorMapper.selectByIdWithCollection(1);
            System.out.println(author);
            System.out.println(author.getBlogList());

            List<Author> authorList=authorMapper.selectAuthorsByIdWithCollection();
            System.out.println(authorList.get(0).getBlogList());
            System.out.println(authorList.get(1).getBlogList());
            Blog blog1 = authorList.get(0).getBlogList().get(0);
            Blog blog2 = authorList.get(1).getBlogList().get(0);
            System.out.println(blog1==blog2);
        }
    }

}