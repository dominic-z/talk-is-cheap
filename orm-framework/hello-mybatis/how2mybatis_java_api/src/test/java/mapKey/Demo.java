package mapKey;

import dao.mapKey.AuthorMapper;
import dao.mapKey.BlogMapper;
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
import java.util.Map;
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

    @Test
    void test() throws IOException {
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        Configuration config = getConfig();
        SqlSessionFactory sqlSessionFactory = builder.build(config);
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);

            Map<Integer,Blog> blogMap = blogMapper.selectBlogsByAuthorId(1);
            System.out.println(blogMap);
        }
    }

}