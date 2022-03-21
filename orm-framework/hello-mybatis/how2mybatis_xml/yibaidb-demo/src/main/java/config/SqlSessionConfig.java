package config;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;

/**
 * @author dominiczhu
 * @version 1.0
 * @title SqlSessionConfig
 * @date 2021/8/31 上午10:19
 */
public class SqlSessionConfig {

    private SqlSessionConfig() {
    }

    private static SqlSessionFactory sqlSessionFactory = null;

    private static final String CONFIG_PATH = "mybatis-config.xml";

    public static SqlSessionFactory getSqlSessionFactory() throws IOException {

        if (sqlSessionFactory != null) {
            return sqlSessionFactory;
        } else {
            synchronized (SqlSessionFactory.class) {
                if (sqlSessionFactory == null) {
                    Reader resource = Resources.getResourceAsReader(CONFIG_PATH);
                    sqlSessionFactory = new SqlSessionFactoryBuilder().build(resource);
                }
                return sqlSessionFactory;
            }
        }

    }

    public static SqlSession getSqlSession(boolean autoCommit) throws IOException {
        return getSqlSessionFactory().openSession(autoCommit);
    }

    public static SqlSession getSqlSession() throws IOException {
        return getSqlSession(false);
    }


}
