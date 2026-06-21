package org.talk.is.cheap.orm.mybatis.hello.config;

import org.apache.ibatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;


/**
 * 我想用Druid作为mybatis的连接池，但是，但是，Druid内置的给mybatis的连接池似乎很久没有更新了。
 * com.alibaba.druid.support.ibatis.DruidDataSourceFactory继承的DataSourceFactory是com.ibatis.sqlmap.engine.datasource.DataSourceFactory
 * 和目前mybatis的DatasourceFactory的路径不匹配
 * <p>
 * 好在代码比较简单，直接抄过来自己实现一个就好
 */
public class MyDruidDataSourceFactory implements DataSourceFactory {
    private DataSource dataSource;


    @Override
    public void setProperties(Properties properties) {
        try {
            this.dataSource = com.alibaba.druid.pool.DruidDataSourceFactory.createDataSource(properties);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("init data source error", e);
        }
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }
}
