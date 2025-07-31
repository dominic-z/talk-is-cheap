package org.talk.is.cheap.orm.mybatis.hello.config;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.talk.is.cheap.orm.mybatis.hello.mappers.BlogMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class SqlSessionConfig {

    private SqlSessionConfig() {
    }

    private static SqlSessionFactory sqlSessionFactory = null;
    private static SqlSession sqlSession = null;

    public static SqlSessionFactory getSqlSessionFactory() throws IOException {

        if (sqlSessionFactory != null) {
            return sqlSessionFactory;
        } else {
            synchronized (SqlSessionConfig.class) {
                if (sqlSessionFactory == null) {
                    Reader resource = Resources.getResourceAsReader("mybatis-config.xml");
                    sqlSessionFactory = new SqlSessionFactoryBuilder().build(resource);
                }
                return sqlSessionFactory;
            }
        }

    }

    public static SqlSession getSqlSession() throws IOException {
        if (sqlSession != null) {
            return sqlSession;
        } else {
            synchronized (SqlSessionConfig.class) {
                if (sqlSession == null) {
                    sqlSession = getSqlSessionFactory().openSession();
                }
                return sqlSession;
            }
        }
    }


    public static <T> T getMapper(Class<T> tClass) throws IOException {
        return getSqlSession().getMapper(tClass);
    }


    /**
     * 通过api配置一个sqlSession
     *
     * @return
     * @throws IOException
     */
    public static SqlSession getSqlSessionByApi() throws IOException {
        Properties jdbcProperties = Resources.getResourceAsProperties("jdbc.properties");
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        DataSource dataSource = new PooledDataSource(
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://localhost:3306/how2mybatis",
                jdbcProperties);
        Environment env = new Environment("production", transactionFactory, dataSource);
        Configuration configuration = new Configuration(env);
//        configuration.setLogImpl(StdOutImpl.class); // 开启日志
        configuration.addMapper(BlogMapper.class);

        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sqlSessionFactory = builder.build(configuration);

        return sqlSessionFactory.openSession();
    }
}
