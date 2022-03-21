package com.example.springboot.hellospringboot.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
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
// 扫描mapper，当然也可以配置在mybatis-config.xml里
// 不过也可以手动构造 如下
@MapperScan(basePackages = {"com.example.springboot.hellospringboot.dao.mbg"},sqlSessionTemplateRef = "yiibaiSqlSessionTemplate")
public class MybatisConfig {

    @Bean
    public SqlSessionFactory yiibaiSqlSessionFactory(@Qualifier("yiibaiDatasource") DataSource dataSource) throws Exception {
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
    public SqlSessionTemplate yiibaiSqlSessionTemplate(@Qualifier("yiibaiSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public DataSourceTransactionManager yiibaiTransactionManager(@Qualifier("yiibaiDatasource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


//    @ConditionalOnMissingBean
//    @Bean
//    @Primary
//    public CustomerMapper customersMapper(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory)
//    throws Exception {
//        // 另一种配置的方式
//
//        MapperFactoryBean<CustomerMapper> mapperFactoryBean = new MapperFactoryBean<>(CustomerMapper.class);
//        mapperFactoryBean.setSqlSessionFactory(sqlSessionFactory);
//        return mapperFactoryBean.getObject();
//    }

//    /**
//     * 这个配置实际上就是MapperScan注解做的事情
//     * @return
//     */
//    @Bean
//    public MapperScannerConfigurer mapperScannerConfigurer(){
//        final MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
//        mapperScannerConfigurer.setBasePackage("com.example.springboot.hellospringboot.dao.mybatis.mbg");
//        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
//        return mapperScannerConfigurer;
//    }
}
