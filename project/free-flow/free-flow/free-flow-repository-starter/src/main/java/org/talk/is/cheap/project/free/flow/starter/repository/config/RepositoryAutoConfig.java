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
import java.io.IOException;


/**
 * 希望依赖这个starter的项目能够自动具备一些数据操作的接口，但不希望影响项目本身自带的数据操作dao接口，所以当前配置需要晚于mybatis、druid等数据库相关的自动配置
 */
@AutoConfiguration(afterName = {"org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
        "org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfiguration",
        "com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure",
        "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration"
})
@Slf4j
@MapperScan(basePackages = "org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg",
        sqlSessionTemplateRef=RepositoryAutoConfig.SQL_SESSION_TEMPLATE_BEAN_NAME)
@ComponentScan(basePackages = {"org.talk.is.cheap.project.free.flow.starter.repository.service",
        "org.talk.is.cheap.project.free.flow.starter.repository.dao.customized"})
public class RepositoryAutoConfig {

    private static final String DATASOURCE_BEAN_NAME = "repositoryStarterDataSource";
    public static final String SQL_SESSION_TEMPLATE_BEAN_NAME = "repositoryStarterSqlSessionTemplate";
    public static final String TRANSACTION_MANAGER_BEAN_NAME = "repositoryStarterTransactionManager";

    // 定制一个名字，不要影响实际应用的bean

    @Bean(name=DATASOURCE_BEAN_NAME)
    public DataSource druidDataSource() throws Exception {

        DataSource dataSource = DruidDataSourceFactory.createDataSource(PropertiesUtil.readFromFile("classpath:/datasource.properties"));
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

        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();

        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLogPrefix("repositoryStarter");
        factory.setConfiguration(configuration);

        return new SqlSessionTemplate(factory.getObject());
    }


    /**
     * 抄的DataSourceTransactionManagerAutoConfiguration
     * @param dataSource
     * @return
     */
    @Bean(TRANSACTION_MANAGER_BEAN_NAME)
    public JdbcTransactionManager jdbcTransactionManager(@Qualifier(DATASOURCE_BEAN_NAME) DataSource dataSource){
        return new JdbcTransactionManager(dataSource);
    }


}
