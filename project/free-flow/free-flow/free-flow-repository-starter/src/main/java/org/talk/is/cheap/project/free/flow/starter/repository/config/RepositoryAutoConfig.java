package org.talk.is.cheap.project.free.flow.starter.repository.config;


import com.alibaba.druid.pool.DruidDataSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.talk.is.cheap.project.free.flow.common.utils.PropertiesUtil;

import javax.sql.DataSource;

/**
 * 这个项目是一个starter项目，希望依赖这个starter的其他项目（下称为应用项目）能够自动具备一些数据操作的接口，但不希望影响应用项目本身自带的数据操作dao接口，
 * 比如应用项目本身有依赖mybatis-starter/druid-starter的话，项目本身会创建mybatis和druid的相关bean，但如果应用项目的容器存在相关bean，
 * 可能mybatis-starter/druid-starter就不会为应用项目自动创建正确的bean了
 * 所以当前配置需要晚于mybatis、druid等数据库相关的自动配置
 *
 * 看的两个starter包确认druid和mybatis的starter配置项
 */
@AutoConfiguration(afterName = {
        "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
        "org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfiguration",
        "com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure",
        "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration"
})
@Slf4j
// MapperScan是mybatis的注解，用来扫对应的mapper对象
@MapperScan(basePackages = "org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg",
        sqlSessionTemplateRef=RepositoryAutoConfig.SQL_SESSION_TEMPLATE_BEAN_NAME)
//ComponentScan就是spring原生的了
@ComponentScan(basePackages = {"org.talk.is.cheap.project.free.flow.starter.repository.service",
        "org.talk.is.cheap.project.free.flow.starter.repository.dao.customized"})
public class RepositoryAutoConfig{

    private static final String DATASOURCE_BEAN_NAME = "repositoryStarterDataSource";
    public static final String SQL_SESSION_TEMPLATE_BEAN_NAME = "repositoryStarterSqlSessionTemplate";
    public static final String TRANSACTION_MANAGER_BEAN_NAME = "repositoryStarterTransactionManager";

    // 定制一个名字，不要影响应用项目里的bean

    @Bean(name=DATASOURCE_BEAN_NAME)
    public DataSource druidDataSource() throws Exception {

        DataSource dataSource = DruidDataSourceFactory.createDataSource(PropertiesUtil.readFromAppClassPath("classpath:/datasource.properties"));
        return dataSource;
    }




    @Bean(SQL_SESSION_TEMPLATE_BEAN_NAME)
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier(DATASOURCE_BEAN_NAME) DataSource dataSource) throws Exception {
        log.info("使用自定义的sqlSessionTemplate");

        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);

        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        factory.setMapperLocations(patternResolver.getResources("classpath:/free-flow-starter-repository-mappers/mappers/**/*.xml"));
        factory.setTypeAliasesPackage("org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo");

        // 代替传统的mybatis.xml配置文件的方式进行配置
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();

        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLogPrefix("repositoryStarter");
        factory.setConfiguration(configuration);

        return new SqlSessionTemplate(factory.getObject());
    }


    /**
     * 抄的DataSourceTransactionManagerAutoConfiguration，至于为啥抄
     * 问的豆包：springboot如何创建一个事务管理器的？
     * https://www.doubao.com/thread/wd77336657ea502f9
     *
     * spring内置的事务管理器被放在了org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration这个位置
     *
     * @param dataSource
     * @return
     */
    @Bean(TRANSACTION_MANAGER_BEAN_NAME)
    public JdbcTransactionManager jdbcTransactionManager(@Qualifier(DATASOURCE_BEAN_NAME) DataSource dataSource){
        return new JdbcTransactionManager(dataSource);
    }


}
