package sqlSessions;

import dao.sqlSession.BlogMapper;
import domain.Blog;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.stdout.StdOutImpl;
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
        Properties jdbcProperties=Resources.getResourceAsProperties("jdbc.properties");
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        DataSource dataSource = new PooledDataSource(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/how2mybatis",
                jdbcProperties);
        Environment env = new Environment("production", transactionFactory, dataSource);
        Configuration configuration = new Configuration(env);
//        configuration.setLogImpl(StdOutImpl.class); // 开启日志
        configuration.getTypeAliasRegistry().registerAlias(Blog.class);
        configuration.getTypeAliasRegistry().registerAliases("domain");
        configuration.getTypeAliasRegistry().registerAliases("wiredDomain");
        configuration.addMapper(BlogMapper.class);
        return configuration;
    }

    @Test
    void test() throws IOException {
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        Configuration config=getConfig();
        SqlSessionFactory sqlSessionFactory = builder.build(config);
//        上面的工作等同于下面代码
//        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
//        SqlSessionFactory sqlSessionFactory = builder.build(Resources.getResourceAsReader("sqlSession/mybatis-config.xml"));
        try(SqlSession sqlSession=sqlSessionFactory.openSession()){
            BlogMapper blogMapper=sqlSession.getMapper(BlogMapper.class);

            Blog blog = blogMapper.selectById(1);
            System.out.println(blog);
            List<Blog> blogs=blogMapper.selectBlogs();
            System.out.println(blogs);
//            下面的操作相当于limit 1 2
            blogs=sqlSession.selectList("dao.sqlSession.BlogMapper.selectBlogs",null,new RowBounds(1,2));
            System.out.println(blogs);

//            下面这段代码我debug了一遍源码，我发现select里的这个resultHandler没用到啊
            sqlSession.select("dao.sqlSession.BlogMapper.selectById", (ResultHandler<Blog>) resultContext -> {
                Blog resultObject = resultContext.getResultObject();
                System.out.println(resultObject);
            });
        }
    }
}
