package codegen.test.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * @author dominiczhu
 * @version 1.0
 * @title MybatisConfig
 * @date 2021/9/15 下午3:32
 */
@Configuration

@MapperScan(basePackages = {"codegen.test.dao"})
public class MybatisConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(@Autowired DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();

        factoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver patternResolver =
                new PathMatchingResourcePatternResolver();
        factoryBean.setConfigLocation(patternResolver.getResource("classpath:/mybatis-config.xml"));
        factoryBean.setMapperLocations(patternResolver.getResources("classpath:/mappers/**/*.xml"));
        factoryBean.setVfs(SpringBootVFS.class); // 为了通过执行jar包启动的时候，找到mapper文件

        return factoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(@Autowired SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public DataSourceTransactionManager TransactionManager(@Autowired DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
