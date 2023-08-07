package org.talk.is.cheap.project.what.to.eat.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@Slf4j
@MapperScan({"org.talk.is.cheap.project.what.to.eat.dao.mbg"}) // 用于扫描Mapper
public class MybatisConfig {

    @Autowired
    private DataSource druidDatasource;
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();

        factoryBean.setDataSource(druidDatasource);
        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        factoryBean.setConfigLocation(patternResolver.getResource("classpath:/mybatis-config.xml"));
        factoryBean.setMapperLocations(patternResolver.getResources("classpath:/mappers/**/*.xml"));
        factoryBean.setVfs(SpringBootVFS.class); // 为了通过执行jar包启动的时候，找到mapper文件
        return factoryBean.getObject();
    }
}
