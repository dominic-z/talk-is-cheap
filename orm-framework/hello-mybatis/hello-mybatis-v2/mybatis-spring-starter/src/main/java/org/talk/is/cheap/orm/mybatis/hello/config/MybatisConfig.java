package org.talk.is.cheap.orm.mybatis.hello.config;


import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.talk.is.cheap.orm.mybatis.hello.mappers.BlogMapper;

import javax.sql.DataSource;


/**
 * 用于创建Mapper对象，并根据mapper对象的注解和xm文件内容进行匹配。
 */
@Configuration
// 测试手动指定某些mapper的sqlSessionTemplate，指定的sqlSessionTemplateRef需要持有BlogMapper的方法对应的mapper xml（即相同的namespace）
@MapperScan(basePackageClasses = BlogMapper.class
//        , sqlSessionTemplateRef = "mySqlSessionTemplate"
)
@Slf4j
public class MybatisConfig {

    /**
     * 下面是手动配置，相当于替代了MybatisAutoConfiguration自动配置
     */

//    @Bean
//    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
//        log.info("使用自定义的sqlSessionFactory");
//        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
//        factory.setDataSource(dataSource);
//        factory.setVfs(SpringBootVFS.class); // 为了通过执行jar包启动的时候，找到mapper文件
//
//        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
//        factory.setConfigLocation(patternResolver.getResource("classpath:/mybatis-config.xml"));
//        factory.setMapperLocations(patternResolver.getResources("classpath:/mappers/**/*.xml"));
//
//        return factory.getObject();
//    }
//
//    @Bean("mySqlSessionTemplate")
//    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
//        log.info("使用自定义的sqlSessionTemplate");
//        return new SqlSessionTemplate(sqlSessionFactory);
//    }


}
